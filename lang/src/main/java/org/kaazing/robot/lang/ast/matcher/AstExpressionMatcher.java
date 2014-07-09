/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package org.kaazing.robot.lang.ast.matcher;

import static org.kaazing.robot.lang.ast.util.AstUtil.equivalent;

import javax.el.ValueExpression;

public class AstExpressionMatcher extends AstValueMatcher {

    private final ValueExpression value;

    public AstExpressionMatcher(ValueExpression value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
    }

    public ValueExpression getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || (obj instanceof AstExpressionMatcher) && equals((AstExpressionMatcher) obj);
    }

    protected boolean equals(AstExpressionMatcher that) {
        return equivalent(this.value, that.value);
    }

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) throws Exception {

        return visitor.visit(this, parameter);
    }

    @Override
    public String toString() {
        return String.format("(%s)%s", value.getExpectedType().getSimpleName(), value.getExpressionString());
    }
}