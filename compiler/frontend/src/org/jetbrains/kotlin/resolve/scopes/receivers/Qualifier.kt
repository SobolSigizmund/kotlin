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

package org.jetbrains.kotlin.resolve.scopes.receivers

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getTopmostParentQualifiedExpressionForSelector
import org.jetbrains.kotlin.resolve.descriptorUtil.classValueType
import org.jetbrains.kotlin.resolve.scopes.ChainedMemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.expressions.isWithoutValueArguments
import java.util.*

interface Qualifier : QualifierReceiver {
    val referenceExpression: KtSimpleNameExpression
}

val Qualifier.expression: KtExpression
    get() = with(referenceExpression) {
        val parent = parent
        // Consider the expression "A.B.C<D>" which can appear in the LHS of '::'. In this case referenceExpression is "C",
        // its parent is the call expression "C<D>", and the topmost qualified expression is "A.B.C<D>"
        val expression = if (parent is KtCallExpression && parent.isWithoutValueArguments) parent else this
        return expression.getTopmostParentQualifiedExpressionForSelector() ?: expression
    }

class PackageQualifier(
        override val referenceExpression: KtSimpleNameExpression,
        override val descriptor: PackageViewDescriptor
) : Qualifier {
    override val classValueReceiver: ReceiverValue? get() = null
    override val staticScope: MemberScope get() = descriptor.memberScope

    override fun toString() = "Package{$descriptor}"
}

abstract class ClassifierQualifier<T : ClassifierDescriptor>(
        override val referenceExpression: KtSimpleNameExpression,
        override val descriptor: T
) : Qualifier

class TypeParameterQualifier(
        referenceExpression: KtSimpleNameExpression,
        descriptor: TypeParameterDescriptor
) : ClassifierQualifier<TypeParameterDescriptor>(referenceExpression, descriptor) {
    override val classValueReceiver: ReceiverValue? get() = null
    override val staticScope: MemberScope get() = MemberScope.Empty

    override fun toString() = "TypeParameter{$descriptor}"
}

/**
 * @param typeArguments type arguments provided for this qualifier, or null if no type arguments are possible in this position.
 *                      Currently this is not null **iff** this qualifier is a part of the qualified expression on the LHS of '::',
 *                      e.g. in "A.B.C<D>::foo" or "C<D>::foo", typeArguments for the qualifier on the reference "C" would be ["D"].
 */
class ClassQualifier(
        referenceExpression: KtSimpleNameExpression,
        descriptor: ClassDescriptor,
        val typeArguments: List<TypeProjection>?
) : ClassifierQualifier<ClassDescriptor>(referenceExpression, descriptor) {
    override val classValueReceiver: ClassValueReceiver? = descriptor.classValueType?.let {
        ClassValueReceiver(this, it)
    }

    override val staticScope: MemberScope get() {
        val scopes = ArrayList<MemberScope>(2)

        scopes.add(descriptor.staticScope)

        if (descriptor.kind != ClassKind.ENUM_ENTRY) {
            scopes.add(descriptor.unsubstitutedInnerClassesScope)
        }

        return ChainedMemberScope("Static scope for ${descriptor.name} as class or object", scopes)
    }

    override fun toString() = "Class{$descriptor}"
}

class ClassValueReceiver(val classQualifier: ClassQualifier, private val type: KotlinType) : ExpressionReceiver {
    override fun getType() = type

    override val expression: KtExpression
        get() = classQualifier.expression
}
