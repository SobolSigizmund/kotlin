/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.types.expressions

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.ReflectionTypes
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.TemporaryBindingTrace
import org.jetbrains.kotlin.resolve.TypeResolver
import org.jetbrains.kotlin.resolve.callableReferences.createReflectionTypeForResolvedCallableReference
import org.jetbrains.kotlin.resolve.callableReferences.resolvePossiblyAmbiguousCallableReference
import org.jetbrains.kotlin.resolve.calls.CallExpressionResolver
import org.jetbrains.kotlin.resolve.calls.CallResolver
import org.jetbrains.kotlin.resolve.calls.callResolverUtil.ResolveArgumentsMode
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.context.TemporaryTraceAndCache
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResults
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResultsUtil
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.scopes.receivers.ClassQualifier
import org.jetbrains.kotlin.resolve.scopes.receivers.ClassifierQualifier
import org.jetbrains.kotlin.types.ErrorUtils
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeImpl
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.createTypeInfo
import javax.inject.Inject

// TODO: use a language level option
val BOUND_REFERENCES_ENABLED by lazy { System.getProperty("kotlin.lang.enable.bound.references") == "true" }

class DoubleColonExpressionResolver(
        val callResolver: CallResolver,
        val callExpressionResolver: CallExpressionResolver,
        val dataFlowAnalyzer: DataFlowAnalyzer,
        val reflectionTypes: ReflectionTypes,
        val typeResolver: TypeResolver
) {
    private lateinit var expressionTypingServices: ExpressionTypingServices

    // component dependency cycle
    @Inject
    fun setExpressionTypingServices(expressionTypingServices: ExpressionTypingServices) {
        this.expressionTypingServices = expressionTypingServices
    }

    fun visitClassLiteralExpression(expression: KtClassLiteralExpression, c: ExpressionTypingContext): KotlinTypeInfo {
        if (expression.isEmptyLHS) {
            // "::class" will maybe mean "this::class", a class of "this" instance
            c.trace.report(UNSUPPORTED.on(expression, "Class literals with empty left hand side are not yet supported"))
        }
        else {
            val result = resolveDoubleColonLHS(expression.receiverExpression!!, expression, c)
            if (result != null) {
                val type = result.type
                if (!type.isError) {
                    if (result is LhsResult.Type) { // TODO: test "Array::class" where 'Array' is a local variable of an Array type
                        checkClassLiteralQualifier(expression, c, type, result.qualifier)
                    }
                    return dataFlowAnalyzer.createCheckedTypeInfo(reflectionTypes.getKClassType(Annotations.EMPTY, type), c, expression)
                }
            }
        }

        return createTypeInfo(ErrorUtils.createErrorType("Unresolved class"), c)
    }

    // Returns true if the expression is not a call expression without value arguments (such as "A<B>") or a qualified expression
    // which contains such call expression as one of its parts.
    // In this case it's pointless to attempt to type check an expression on the LHS in "A<B>::class", since "A<B>" certainly means a type.
    private fun KtExpression.canBeConsideredProperExpression(): Boolean {
        return when (this) {
            is KtCallExpression ->
                !isWithoutValueArguments
            is KtDotQualifiedExpression ->
                receiverExpression.canBeConsideredProperExpression() &&
                selectorExpression?.let { it.canBeConsideredProperExpression() } ?: false
            else -> true
        }
    }

    sealed class LhsResult(val type: KotlinType) {
        class Expression(
                val typeInfo: KotlinTypeInfo
        ) : LhsResult(typeInfo.type!!)

        class Type(
                type: KotlinType,
                val qualifier: ClassifierQualifier<*>
        ) : LhsResult(type)
    }

    private fun resolveDoubleColonLHS(
            expression: KtExpression, doubleColonExpression: KtDoubleColonExpression, c: ExpressionTypingContext
    ): LhsResult? {
        // First, try resolving the LHS as expression, if possible

        if (BOUND_REFERENCES_ENABLED && expression.canBeConsideredProperExpression() &&
            !doubleColonExpression.hasQuestionMarks /* TODO: test this */) {
            val traceForExpr = TemporaryTraceAndCache.create(c, "resolve '::' LHS as expression", expression)
            val contextForExpr = c.replaceTraceAndCache(traceForExpr)
            val typeInfo = expressionTypingServices.getTypeInfo(expression, contextForExpr)
            val type = typeInfo.type
            // TODO (!!!): it's wrong to only check type, should check that there's a companion qualifier
            if (type != null && !DescriptorUtils.isCompanionObject(type.constructor.declarationDescriptor)) {
                traceForExpr.commit()
                return LhsResult.Expression(typeInfo)
            }
        }

        // Then, try resolving it as type

        when {
            expression is KtSimpleNameExpression ->
                callExpressionResolver.resolveSimpleDoubleColonLHS(expression, emptyList(), c, receiver = null)
            expression is KtCallExpression && expression.isWithoutValueArguments && expression.calleeExpression is KtSimpleNameExpression ->
                callExpressionResolver.resolveSimpleDoubleColonLHS(
                        expression.calleeExpression as KtSimpleNameExpression, expression.typeArguments, c, receiver = null
                )
            expression is KtDotQualifiedExpression ->
                callExpressionResolver.getQualifiedExpressionTypeInfo(expression, c, isDoubleColonLHS = true)
        }

        val qualifier = c.trace.bindingContext.get(BindingContext.QUALIFIER, expression)
        if (qualifier !is ClassifierQualifier<*>) return null
        val target = qualifier.descriptor
        if (ErrorUtils.isError(target)) return null

        val arguments =
                // TODO: consider also checking bounds; see the similar code in TypeResolver
                if (qualifier is ClassQualifier && qualifier.typeArguments?.size == target.typeConstructor.parameters.size) {
                    qualifier.typeArguments!!
                }
                else target.typeConstructor.parameters.map(TypeUtils::makeStarProjection)

        val type = KotlinTypeImpl.create(
                Annotations.EMPTY, target.typeConstructor, doubleColonExpression.hasQuestionMarks, arguments,
                (target as? ClassDescriptor)?.getMemberScope(arguments) ?: target.defaultType.memberScope
        )

        return LhsResult.Type(type, qualifier)
    }

    // TODO: report better diagnostics ("no type arguments expected", "type parameter is not reified", ...)
    private fun checkClassLiteralQualifier(
            expression: KtClassLiteralExpression,
            c: ExpressionTypingContext,
            type: KotlinType,
            qualifier: ClassifierQualifier<*>
    ) {
        val descriptor = qualifier.descriptor
        if (descriptor is ClassDescriptor && KotlinBuiltIns.isNonPrimitiveArray(descriptor)) {
            if ((qualifier as? ClassQualifier)?.typeArguments?.isEmpty() ?: true) { // TODO: test kotlin<Foo>.Array
                c.trace.report(ARRAY_CLASS_LITERAL_REQUIRES_ARGUMENT.on(expression))
            }
            else if (!isAllowedInClassLiteral(type)) {
                c.trace.report(CLASS_LITERAL_LHS_NOT_A_CLASS.on(expression))
            }

            return
        }

        if (expression.hasTypeArgumentsInQualifiers() || expression.hasQuestionMarks ||
            (descriptor is TypeParameterDescriptor && !descriptor.isReified)) {
            c.trace.report(CLASS_LITERAL_LHS_NOT_A_CLASS.on(expression))
        }
    }

    private fun KtClassLiteralExpression.hasTypeArgumentsInQualifiers(): Boolean {
        var expression = receiverExpression ?: return false
        while (true) {
            when (expression) {
                is KtCallExpression -> return true
                is KtDotQualifiedExpression -> {
                    if (expression.selectorExpression is KtCallExpression) return true
                    expression = expression.receiverExpression
                }
                else -> return false
            }
        }
    }

    private fun isAllowedInClassLiteral(type: KotlinType): Boolean {
        val typeConstructor = type.constructor
        val descriptor = typeConstructor.declarationDescriptor

        when (descriptor) {
            is ClassDescriptor -> {
                if (KotlinBuiltIns.isNonPrimitiveArray(descriptor)) {
                    return type.arguments.none { typeArgument ->
                        typeArgument.isStarProjection || !isAllowedInClassLiteral(typeArgument.type)
                    }
                }

                return type.arguments.isEmpty()
            }
            is TypeParameterDescriptor -> return descriptor.isReified
            else -> return false
        }
    }

    fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression, c: ExpressionTypingContext): KotlinTypeInfo {
        val callableReference = expression.callableReference
        if (callableReference.getReferencedName().isEmpty()) {
            expression.receiverExpression?.let { resolveDoubleColonLHS(it, expression, c) }
            c.trace.report(UNRESOLVED_REFERENCE.on(callableReference, callableReference))
            val errorType = ErrorUtils.createErrorType("Empty callable reference")
            return dataFlowAnalyzer.createCheckedTypeInfo(errorType, c, expression)
        }

        val trace = TemporaryBindingTrace.create(c.trace, "Callable reference type")
        val context = c.replaceBindingTrace(trace)
        val (lhsResult, resolutionResults) = resolveCallableReference(expression, context, ResolveArgumentsMode.RESOLVE_FUNCTION_ARGUMENTS)
        val result = getCallableReferenceType(expression, lhsResult, resolutionResults, context)
        val hasErrors = hasErrors(trace) // Do not inline this local variable (execution order is important)
        trace.commit()
        if (!hasErrors && result != null) {
            checkNoExpressionOnLHS(expression, c)
        }
        return dataFlowAnalyzer.createCheckedTypeInfo(result, c, expression)
    }

    private fun hasErrors(trace: TemporaryBindingTrace): Boolean =
            trace.bindingContext.diagnostics.all().any { diagnostic -> diagnostic.severity == Severity.ERROR }

    private fun checkNoExpressionOnLHS(expression: KtCallableReferenceExpression, c: ExpressionTypingContext) {
        val typeReference = expression.typeReference ?: return
        var typeElement = typeReference.typeElement as? KtUserType ?: return

        while (true) {
            if (typeElement.typeArgumentList != null) return
            typeElement = typeElement.qualifier ?: break
        }

        val simpleNameExpression = typeElement.referenceExpression ?: return

        val traceAndCache = TemporaryTraceAndCache.create(c, "Resolve expression on LHS of callable reference", simpleNameExpression)
        val resolutionResult = callExpressionResolver.resolveSimpleName(c.replaceTraceAndCache(traceAndCache), simpleNameExpression)

        val resultingCalls = resolutionResult.resultingCalls.filter { call ->
            call.status.possibleTransformToSuccess() && !ErrorUtils.isError(call.resultingDescriptor)
        }
        if (resultingCalls.isEmpty()) return

        if (resultingCalls.singleOrNull()?.resultingDescriptor is FakeCallableDescriptorForObject) return

        throw AssertionError(String.format(
                "Expressions on left-hand side of callable reference are not supported yet.\n" +
                "Resolution result: %s\n" +
                "Original result: %s",
                resultingCalls.map { call -> call.resultingDescriptor },
                expression.callableReference.getResolvedCall(c.trace.bindingContext)?.resultingDescriptor
        ))
    }

    private fun getCallableReferenceType(
            expression: KtCallableReferenceExpression,
            lhsResult: LhsResult?,
            resolutionResults: OverloadResolutionResults<*>?,
            context: ExpressionTypingContext
    ): KotlinType? {
        val reference = expression.callableReference

        val descriptor =
                if (resolutionResults != null && !resolutionResults.isNothing) {
                    OverloadResolutionResultsUtil.getResultingCall(resolutionResults, context.contextDependency)?.let { call ->
                        call.resultingDescriptor
                    } ?: return null
                }
                else {
                    context.trace.report(UNRESOLVED_REFERENCE.on(reference, reference))
                    return null
                }

        if (expression.isEmptyLHS &&
            (descriptor.dispatchReceiverParameter != null || descriptor.extensionReceiverParameter != null)) {
            context.trace.report(CALLABLE_REFERENCE_TO_MEMBER_OR_EXTENSION_WITH_EMPTY_LHS.on(reference))
        }

        val containingDeclaration = descriptor.containingDeclaration
        if (DescriptorUtils.isObject(containingDeclaration)) {
            context.trace.report(CALLABLE_REFERENCE_TO_OBJECT_MEMBER.on(reference))
        }
        if (descriptor is ConstructorDescriptor && DescriptorUtils.isAnnotationClass(containingDeclaration)) {
            context.trace.report(CALLABLE_REFERENCE_TO_ANNOTATION_CONSTRUCTOR.on(reference))
        }

        val ignoreReceiver = lhsResult is LhsResult.Expression || expression.isEmptyLHS
        return createReflectionTypeForResolvedCallableReference(
                expression, lhsResult?.type, ignoreReceiver, descriptor, context, reflectionTypes
        )
    }

    fun resolveCallableReference(
            expression: KtCallableReferenceExpression,
            context: ExpressionTypingContext,
            resolveArgumentsMode: ResolveArgumentsMode
    ): Pair<LhsResult?, OverloadResolutionResults<*>?> {
        val lhsResult = expression.receiverExpression?.let { resolveDoubleColonLHS(it, expression, context) }

        val resolutionResults = resolvePossiblyAmbiguousCallableReference(
                expression, lhsResult?.type, context, resolveArgumentsMode, callResolver
        )

        return lhsResult to resolutionResults
    }
}

val KtCallExpression.isWithoutValueArguments: Boolean
    get() = valueArgumentList == null && lambdaArguments.isEmpty() && typeArgumentList != null
