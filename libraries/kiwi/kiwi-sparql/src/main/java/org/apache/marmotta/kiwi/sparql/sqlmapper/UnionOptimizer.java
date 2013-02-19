/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.lmf.sparql.services.evaluation.sql;

import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Move unions that are contained in Joins or LeftJoins out (switch positions of union and join)
 * <p/>
 * Author: Sebastian Schaffert
 */
public class UnionOptimizer  extends QueryModelVisitorBase<RuntimeException> {

    public UnionOptimizer() {
    }

    public void optimize(TupleExpr expr) {
        expr.visit(this);
    }


    // rewrite multiprojection so that each projection gets a unique name and turn it into a projection


    @Override
    public void meet(MultiProjection node) throws RuntimeException {
        int projectionCounter = 0;

        List<ProjectionElem> projectionElemList = new ArrayList<ProjectionElem>();
        for(ProjectionElemList elems : node.getProjections()) {
            projectionCounter++;

            for(ProjectionElem elem : elems.getElements()) {
                ProjectionElem celem = elem.clone();
                celem.setTargetName("_multi_"+elem.getTargetName()+"_"+projectionCounter);
                projectionElemList.add(celem);
            }
        }
        ProjectionElemList projections = new ProjectionElemList(projectionElemList);

        Projection projection = new Projection(node.getArg().clone(),projections);
        node.replaceWith(projection);
        projection.visit(this);
    }

    @Override
    public void meet(Union node) throws RuntimeException {
        if(node.getParentNode() instanceof Join || node.getParentNode() instanceof LeftJoin) {
            // move the other arg of the join inside the union
            BinaryTupleOperator parent = (BinaryTupleOperator)node.getParentNode();
            if(node == parent.getLeftArg()) {
                // transform Join(Union(X,Y),Z) into Union(Join(X,Z),Join(Y,Z))

                // parent is the join, so we create two copies of it
                BinaryTupleOperator join1 = parent.clone();
                BinaryTupleOperator join2 = parent.clone();

                // take the left argument of the union and the right argument of the join, and put them into join1
                join1.setLeftArg(node.getLeftArg());
                join1.setRightArg(parent.getRightArg());

                // take the right argument of the union and the right argument of the join, and put them into join2
                join2.setLeftArg(node.getRightArg());
                join2.setRightArg(parent.getRightArg());

                // copy the union node to a new union node, and set the two new joins as arguments
                Union union = node.clone();
                union.setLeftArg(join1);
                union.setRightArg(join2);

                // replace parent by the new union
                parent.replaceWith(union);

                union.visit(this);
            } else if(node == parent.getRightArg()) {
                // transform Join(X,Union(Y,Z)) into Union(Join(X,Y),Join(X,Z))

                // parent is the join, so we create two copies of it
                BinaryTupleOperator join1 = parent.clone();
                BinaryTupleOperator join2 = parent.clone();

                // take the the left argument of the join and the left argument of the union , and put them into join1
                join1.setLeftArg(parent.getLeftArg());
                join1.setRightArg(node.getLeftArg());

                // take the the left argument of the join and the right argument of the union , and put them into join1
                join2.setLeftArg(parent.getLeftArg());
                join2.setRightArg(node.getRightArg());

                // copy the union node to a new union node, and set the two new joins as arguments
                Union union = node.clone();
                union.setLeftArg(join1);
                union.setRightArg(join2);

                // replace parent by the new union
                parent.replaceWith(union);

                union.visit(this);
            }

        }
    }

    @Override
    public void meet(Intersection node) throws RuntimeException {
        if(node.getParentNode() instanceof Join || node.getParentNode() instanceof LeftJoin) {
            // move the other arg of the join inside the union
            BinaryTupleOperator parent = (BinaryTupleOperator)node.getParentNode();
            if(node == parent.getLeftArg()) {
                // transform Join(Union(X,Y),Z) into Union(Join(X,Z),Join(Y,Z))

                // parent is the join, so we create two copies of it
                BinaryTupleOperator join1 = parent.clone();
                BinaryTupleOperator join2 = parent.clone();

                // take the left argument of the union and the right argument of the join, and put them into join1
                join1.setLeftArg(node.getLeftArg());
                join1.setRightArg(parent.getRightArg());

                // take the right argument of the union and the right argument of the join, and put them into join2
                join2.setLeftArg(node.getRightArg());
                join2.setRightArg(parent.getRightArg());

                // copy the union node to a new union node, and set the two new joins as arguments
                Intersection intersection = node.clone();
                intersection.setLeftArg(join1);
                intersection.setRightArg(join2);

                // replace parent by the new union
                parent.replaceWith(intersection);

                intersection.visit(this);
            } else if(node == parent.getRightArg()) {
                // transform Join(X,Union(Y,Z)) into Union(Join(X,Y),Join(X,Z))

                // parent is the join, so we create two copies of it
                BinaryTupleOperator join1 = parent.clone();
                BinaryTupleOperator join2 = parent.clone();

                // take the the left argument of the join and the left argument of the union , and put them into join1
                join1.setLeftArg(parent.getLeftArg());
                join1.setRightArg(node.getLeftArg());

                // take the the left argument of the join and the right argument of the union , and put them into join1
                join2.setLeftArg(parent.getLeftArg());
                join2.setRightArg(node.getRightArg());

                // copy the union node to a new union node, and set the two new joins as arguments
                Intersection intersection = node.clone();
                intersection.setLeftArg(join1);
                intersection.setRightArg(join2);

                // replace parent by the new union
                parent.replaceWith(intersection);

                intersection.visit(this);
            }

        }
    }

    @Override
    public void meet(Difference node) throws RuntimeException {
        if(node.getParentNode() instanceof Join || node.getParentNode() instanceof LeftJoin) {
            // move the other arg of the join inside the union
            BinaryTupleOperator parent = (BinaryTupleOperator)node.getParentNode();
            if(node == parent.getLeftArg()) {
                // transform Join(Union(X,Y),Z) into Union(Join(X,Z),Join(Y,Z))

                // parent is the join, so we create two copies of it
                BinaryTupleOperator join1 = parent.clone();
                BinaryTupleOperator join2 = parent.clone();

                // take the left argument of the union and the right argument of the join, and put them into join1
                join1.setLeftArg(node.getLeftArg());
                join1.setRightArg(parent.getRightArg());

                // take the right argument of the union and the right argument of the join, and put them into join2
                join2.setLeftArg(node.getRightArg());
                join2.setRightArg(parent.getRightArg());

                // copy the union node to a new union node, and set the two new joins as arguments
                Difference difference = node.clone();
                difference.setLeftArg(join1);
                difference.setRightArg(join2);

                // replace parent by the new union
                parent.replaceWith(difference);

                difference.visit(this);
            } else if(node == parent.getRightArg()) {
                // transform Join(X,Union(Y,Z)) into Union(Join(X,Y),Join(X,Z))

                // parent is the join, so we create two copies of it
                BinaryTupleOperator join1 = parent.clone();
                BinaryTupleOperator join2 = parent.clone();

                // take the the left argument of the join and the left argument of the union , and put them into join1
                join1.setLeftArg(parent.getLeftArg());
                join1.setRightArg(node.getLeftArg());

                // take the the left argument of the join and the right argument of the union , and put them into join1
                join2.setLeftArg(parent.getLeftArg());
                join2.setRightArg(node.getRightArg());

                // copy the union node to a new union node, and set the two new joins as arguments
                Difference difference = node.clone();
                difference.setLeftArg(join1);
                difference.setRightArg(join2);

                // replace parent by the new union
                parent.replaceWith(difference);

                difference.visit(this);
            }

        }
    }

}
