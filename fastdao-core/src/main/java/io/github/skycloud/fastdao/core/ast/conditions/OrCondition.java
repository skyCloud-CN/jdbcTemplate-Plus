/**
 * @(#)OrCondition.java, 10月 20, 2019.
 * <p>
 * Copyright 2019 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package io.github.skycloud.fastdao.core.ast.conditions;

import com.google.common.collect.Lists;
import io.github.skycloud.fastdao.core.ast.Condition;
import io.github.skycloud.fastdao.core.ast.SqlAst;
import io.github.skycloud.fastdao.core.ast.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuntian
 */
public interface OrCondition<T extends OrCondition> extends Condition {

    T or(Condition condition);

    T orOptional(Condition condition);

    T orIf(Condition condition,boolean preCondition);
    /**
     * this method is for dynamic SQL when there is no subCondition.
     * if this method is executed,request will translate to SQL `SELECT * FROM table`
     * or else ,request will be seen as a illegal request and return nothing
     */
    T allowEmpty();

    /**
     * @author yuntian
     */

    class OrConditionAst<T extends OrCondition> implements OrCondition<T>, SqlAst {

        private List<Condition> subConditions = Lists.newArrayList();

        private boolean allowEmpty = false;

        @Override
        public T or(Condition condition) {
            subConditions.add(condition);
            return (T) this;
        }

        @Override
        public T orOptional(Condition condition) {
            if (condition.isLegal()) {
                subConditions.add(condition);
            }
            return (T) this;
        }

        @Override
        public T orIf(Condition condition,boolean preCondition) {
            if(preCondition){
                subConditions.add(condition);
            }
            return (T)this;
        }

        @Override
        public T allowEmpty() {
            allowEmpty = true;
            return (T) this;
        }

        @Override
        public boolean isLegal() {
            // all subCondition must be legal
            for (Condition subCondition : subConditions) {
                if (!subCondition.isLegal()) {
                    return false;
                }
            }
            return allowEmpty || !isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return subConditions.isEmpty() || subConditions.stream().anyMatch(Condition::isEmpty);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public SqlAst copy() {
            List<Condition> copySubConditions = new ArrayList<>(subConditions.size());
            for (Condition condition : subConditions) {
                copySubConditions.add((Condition) ((SqlAst) condition).copy());
            }
            OrConditionAst orCondition = new OrConditionAst();
            orCondition.subConditions = copySubConditions;
            orCondition.allowEmpty = allowEmpty;
            return orCondition;
        }

        public List<Condition> getSubConditions() {
            return subConditions;
        }
    }
}