/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.dmn.client.editors.expressions.commands;

import javax.enterprise.event.Event;

import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.model.Expression;
import org.kie.workbench.common.dmn.api.definition.model.FunctionDefinition;
import org.kie.workbench.common.dmn.client.editors.expressions.ExpressionEditorView;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.FunctionProps;
import org.kie.workbench.common.dmn.client.widgets.grid.model.ExpressionEditorChanged;

import static org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.util.ExpressionModelFiller.fillFunctionExpression;

public class FillFunctionExpressionCommand extends FillExpressionCommand<FunctionProps> {

    public FillFunctionExpressionCommand(final HasExpression hasExpression,
                                         final FunctionProps expressionProps,
                                         final Event<ExpressionEditorChanged> editorSelectedEvent,
                                         final String nodeUUID,
                                         final ExpressionEditorView view) {
        super(hasExpression, expressionProps, editorSelectedEvent, nodeUUID, view);
    }

    @Override
    protected void fill() {
        final FunctionDefinition function = (FunctionDefinition) getHasExpression().getExpression();
        fill(function, getExpressionProps());
    }

    @Override
    protected Expression getTemporaryExpression() {
        final FunctionDefinition functionDefinition = (FunctionDefinition) getNewExpression();
        fill(functionDefinition, getExpressionProps());
        return functionDefinition;
    }

    @Override
    protected Expression getNewExpression() {
        return new FunctionDefinition();
    }

    void fill(final FunctionDefinition expression, final FunctionProps props) {
        fillFunctionExpression(expression, props);
    }
}
