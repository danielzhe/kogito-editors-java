/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.editors.expressions;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.ait.lienzo.client.core.mediator.Mediators;
import com.ait.lienzo.client.core.shape.Viewport;
import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.test.LienzoMockitoTestRunner;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import elemental2.dom.DOMTokenList;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.model.Expression;
import org.kie.workbench.common.dmn.api.definition.model.LiteralExpression;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.commands.factory.DefaultCanvasCommandFactory;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.ClearExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillContextExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillDecisionTableExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillFunctionExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillInvocationExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillListExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillLiteralExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.commands.FillRelationExpressionCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.ContextProps;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.DecisionTableProps;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.ExpressionProps;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.FunctionProps;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.InvocationProps;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.ListProps;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.LiteralProps;
import org.kie.workbench.common.dmn.client.editors.expressions.jsinterop.props.RelationProps;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinition;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinitions;
import org.kie.workbench.common.dmn.client.editors.expressions.types.function.supplementary.pmml.PMMLDocumentMetadataProvider;
import org.kie.workbench.common.dmn.client.editors.expressions.util.UserActionChecker;
import org.kie.workbench.common.dmn.client.session.DMNEditorSession;
import org.kie.workbench.common.dmn.client.widgets.grid.BaseExpressionGrid;
import org.kie.workbench.common.dmn.client.widgets.grid.ExpressionGridCache;
import org.kie.workbench.common.dmn.client.widgets.grid.ExpressionGridCacheImpl;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.container.CellEditorControlsView;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.ListSelectorView;
import org.kie.workbench.common.dmn.client.widgets.grid.keyboard.KeyboardOperationEscapeGridCell;
import org.kie.workbench.common.dmn.client.widgets.grid.model.ExpressionEditorChanged;
import org.kie.workbench.common.dmn.client.widgets.grid.model.GridCellTuple;
import org.kie.workbench.common.dmn.client.widgets.layer.DMNGridLayer;
import org.kie.workbench.common.dmn.client.widgets.panel.DMNGridPanel;
import org.kie.workbench.common.dmn.client.widgets.panel.DMNGridPanelContainer;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.DomainObjectSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommand;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.command.impl.CompositeCommand;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.processing.index.Index;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseGridData;
import org.uberfire.ext.wires.core.grids.client.widget.grid.GridWidget;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.BaseGridWidgetKeyboardHandler;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.KeyboardOperation;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.KeyboardOperationEditCell;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.KeyboardOperationInvokeContextMenuForSelectedCell;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.KeyboardOperationMoveDown;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.KeyboardOperationMoveLeft;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.KeyboardOperationMoveRight;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.KeyboardOperationMoveUp;
import org.uberfire.ext.wires.core.grids.client.widget.layer.pinning.TransformMediator;
import org.uberfire.ext.wires.core.grids.client.widget.layer.pinning.impl.RestrictedMousePanMediator;
import org.uberfire.mocks.EventSourceMock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.workbench.common.dmn.client.editors.expressions.ExpressionEditorViewImpl.ENABLED_BETA_CSS_CLASS;
import static org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionType.LITERAL_EXPRESSION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LienzoMockitoTestRunner.class)
public class ExpressionEditorViewImplTest {

    private static final String NODE_UUID = "uuid";
    private static final String UNDEFINED_EXPRESSION_DEFINITION_NAME = "Undefined";
    private static final String NAME = "name";

    @Mock
    private Anchor returnToLink;

    @Mock
    private Span expressionName;

    @Mock
    private Span expressionType;

    @Mock
    private DMNGridPanel gridPanel;

    @Mock
    private DMNGridLayer gridLayer;

    @Mock
    private RestrictedMousePanMediator mousePanMediator;

    @Mock
    private CellEditorControlsView.Presenter cellEditorControls;

    @Mock
    private ExpressionEditorView.Presenter presenter;

    @Mock
    private TranslationService translationService;

    @Mock
    private ListSelectorView.Presenter listSelector;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private DMNEditorSession session;

    @Mock
    private SessionCommandManager<AbstractCanvasHandler> sessionCommandManager;

    @Mock
    private DefaultCanvasCommandFactory canvasCommandFactory;

    @Mock
    private Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier;

    @Mock
    private EventSourceMock<RefreshFormPropertiesEvent> refreshFormPropertiesEvent;

    @Mock
    private EventSourceMock<DomainObjectSelectionEvent> domainObjectSelectionEvent;

    @Mock
    private EventSourceMock<ExpressionEditorChanged> editorSelectedEvent;

    @Mock
    private PMMLDocumentMetadataProvider pmmlDocumentMetadataProvider;

    @Mock
    private DefinitionUtils definitionUtils;

    @Mock
    private ExpressionEditorDefinition undefinedExpressionEditorDefinition;

    @Mock
    private BaseExpressionGrid undefinedExpressionEditor;

    @Mock
    private ExpressionEditorDefinition literalExpressionEditorDefinition;

    @Mock
    private BaseExpressionGrid literalExpressionEditor;

    @Mock
    private Viewport viewport;

    @Mock
    private Element gridPanelElement;

    @Mock
    private Mediators viewportMediators;

    @Mock
    private ExpressionEditorDefinition<Expression> editorDefinition;

    @Mock
    private BaseExpressionGrid editor;

    @Mock
    private HasExpression hasExpression;

    @Mock
    private HTMLAnchorElement tryIt;

    @Mock
    private HTMLAnchorElement switchBack;

    @Mock
    private HTMLDivElement betaBoxedExpressionToggle;

    @Mock
    private HTMLDivElement newBoxedExpression;

    @Mock
    private HTMLDivElement dmnExpressionType;

    @Mock
    private HTMLDivElement dmnExpressionEditor;

    @Mock
    private AbstractCanvasHandler canvasHandler;

    @Mock
    private UserActionChecker userActionChecker;

    @Captor
    private ArgumentCaptor<Transform> transformArgumentCaptor;

    @Captor
    private ArgumentCaptor<GridWidget> expressionContainerArgumentCaptor;

    @Captor
    private ArgumentCaptor<TransformMediator> transformMediatorArgumentCaptor;

    @Captor
    private ArgumentCaptor<KeyboardOperation> keyboardOperationArgumentCaptor;

    @Captor
    private ArgumentCaptor<FillExpressionCommand> commandCaptor;

    private ExpressionGridCache expressionGridCache;

    private DMNGridPanelContainer gridPanelContainer;

    private ExpressionEditorViewImpl view;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        this.expressionGridCache = new ExpressionGridCacheImpl();
        this.gridPanelContainer = spy(new DMNGridPanelContainer());
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getExpressionGridCache()).thenReturn(expressionGridCache);
        when(session.getGridPanel()).thenReturn(gridPanel);
        when(session.getGridLayer()).thenReturn(gridLayer);
        when(session.getCellEditorControls()).thenReturn(cellEditorControls);
        when(session.getMousePanMediator()).thenReturn(mousePanMediator);
        when(session.getCanvasHandler()).thenReturn(canvasHandler);

        doReturn(viewport).when(gridPanel).getViewport();
        doReturn(viewportMediators).when(viewport).getMediators();
        doReturn(gridPanelElement).when(gridPanel).getElement();
        doReturn(Optional.of(editor)).when(editorDefinition).getEditor(any(GridCellTuple.class),
                                                                       any(Optional.class),
                                                                       any(HasExpression.class),
                                                                       any(Optional.class),
                                                                       anyBoolean(),
                                                                       anyInt());
        doReturn(new BaseGridData()).when(editor).getModel();

        this.view = spy(new ExpressionEditorViewImpl(returnToLink,
                                                     expressionName,
                                                     expressionType,
                                                     gridPanelContainer,
                                                     translationService,
                                                     listSelector,
                                                     sessionManager,
                                                     sessionCommandManager,
                                                     canvasCommandFactory,
                                                     expressionEditorDefinitionsSupplier,
                                                     refreshFormPropertiesEvent,
                                                     domainObjectSelectionEvent,
                                                     editorSelectedEvent,
                                                     pmmlDocumentMetadataProvider,
                                                     definitionUtils,
                                                     tryIt,
                                                     switchBack,
                                                     betaBoxedExpressionToggle,
                                                     newBoxedExpression,
                                                     dmnExpressionType,
                                                     dmnExpressionEditor,
                                                     userActionChecker));
        view.init(presenter);
        view.bind(session);

        doReturn(hasExpression).when(view).getHasExpression();
        doReturn(editorSelectedEvent).when(view).getEditorSelectedEvent();
        doReturn(NODE_UUID).when(view).getNodeUUID();

        final ExpressionEditorDefinitions expressionEditorDefinitions = new ExpressionEditorDefinitions();
        expressionEditorDefinitions.add(undefinedExpressionEditorDefinition);
        expressionEditorDefinitions.add(literalExpressionEditorDefinition);

        when(expressionEditorDefinitionsSupplier.get()).thenReturn(expressionEditorDefinitions);
        when(undefinedExpressionEditorDefinition.getModelClass()).thenReturn(Optional.empty());
        when(undefinedExpressionEditorDefinition.getName()).thenReturn(UNDEFINED_EXPRESSION_DEFINITION_NAME);
        when(undefinedExpressionEditor.getModel()).thenReturn(new BaseGridData());
        when(undefinedExpressionEditorDefinition.getEditor(any(GridCellTuple.class),
                                                           any(Optional.class),
                                                           any(HasExpression.class),
                                                           any(Optional.class),
                                                           anyBoolean(),
                                                           anyInt())).thenReturn(Optional.of(undefinedExpressionEditor));

        when(literalExpressionEditorDefinition.getModelClass()).thenReturn(Optional.of(new LiteralExpression()));
        when(literalExpressionEditorDefinition.getName()).thenReturn(LITERAL_EXPRESSION.getText());
        when(literalExpressionEditor.getModel()).thenReturn(new BaseGridData());
        when(literalExpressionEditorDefinition.getEditor(any(GridCellTuple.class),
                                                         any(Optional.class),
                                                         any(HasExpression.class),
                                                         any(Optional.class),
                                                         anyBoolean(),
                                                         anyInt())).thenReturn(Optional.of(literalExpressionEditor));

        doAnswer((i) -> i.getArguments()[1]).when(translationService).format(Mockito.<String>any(), anyObject());
        doAnswer((i) -> i.getArguments()[0]).when(translationService).getTranslation(Mockito.<String>any());

        betaBoxedExpressionToggle.classList = mock(DOMTokenList.class);
        newBoxedExpression.classList = mock(DOMTokenList.class);
        dmnExpressionType.classList = mock(DOMTokenList.class);
        dmnExpressionEditor.classList = mock(DOMTokenList.class);
    }

    @Test
    public void testBind() {
        //ExpressionEditorViewImpl.bind(..) is called in @Before setup
        verify(view).setupGridPanel();
        verify(view).setupGridWidget();
        verify(view).setupGridWidgetPanControl();
    }

    @Test
    public void testSetupGridPanel() {
        verify(viewport).setTransform(transformArgumentCaptor.capture());
        final Transform transform = transformArgumentCaptor.getValue();

        assertEquals(ExpressionEditorViewImpl.VP_SCALE,
                     transform.getScaleX(),
                     0.0);
        assertEquals(ExpressionEditorViewImpl.VP_SCALE,
                     transform.getScaleY(),
                     0.0);

        verify(gridPanel).addKeyDownHandler(any(BaseGridWidgetKeyboardHandler.class));
        verify(gridPanelContainer).clear();
        verify(gridPanelContainer).setWidget(gridPanel);

        verify(view, times(7)).addKeyboardOperation(any(BaseGridWidgetKeyboardHandler.class),
                                                    keyboardOperationArgumentCaptor.capture());
        final List<KeyboardOperation> operations = keyboardOperationArgumentCaptor.getAllValues();
        assertThat(operations.get(0)).isInstanceOf(KeyboardOperationEditCell.class);
        assertThat(operations.get(1)).isInstanceOf(KeyboardOperationEscapeGridCell.class);
        assertThat(operations.get(2)).isInstanceOf(KeyboardOperationMoveLeft.class);
        assertThat(operations.get(3)).isInstanceOf(KeyboardOperationMoveRight.class);
        assertThat(operations.get(4)).isInstanceOf(KeyboardOperationMoveUp.class);
        assertThat(operations.get(5)).isInstanceOf(KeyboardOperationMoveDown.class);
        assertThat(operations.get(6)).isInstanceOf(KeyboardOperationInvokeContextMenuForSelectedCell.class);
    }

    @Test
    public void testSetupGridWidget() {
        verify(gridLayer).removeAll();
        verify(gridLayer).add(expressionContainerArgumentCaptor.capture());

        final GridWidget expressionContainer = expressionContainerArgumentCaptor.getValue();

        verify(gridLayer).select(eq(expressionContainer));
        verify(gridLayer).enterPinnedMode(eq(expressionContainer),
                                          any(Command.class));
    }

    @Test
    public void testSetupGridWidgetPanControl() {
        verify(mousePanMediator).setTransformMediator(transformMediatorArgumentCaptor.capture());

        final TransformMediator transformMediator = transformMediatorArgumentCaptor.getValue();

        verify(mousePanMediator).setBatchDraw(true);
        verify(gridLayer).setDefaultTransformMediator(eq(transformMediator));
        verify(viewportMediators).push(eq(mousePanMediator));
    }

    @Test
    public void testOnResize() {
        view.onResize();

        verify(gridPanelContainer).onResize();
        verify(gridPanel).onResize();
    }

    @Test
    public void testSetReturnToLinkText() {
        final String RETURN_LINK = "return-link";

        view.setReturnToLinkText(RETURN_LINK);

        verify(returnToLink).setTextContent(eq(RETURN_LINK));
    }

    @Test
    public void testSetExpression() {
        final Optional<HasName> hasName = Optional.empty();

        view.setExpression(NODE_UUID,
                           hasExpression,
                           hasName,
                           false);

        verify(gridLayer).add(expressionContainerArgumentCaptor.capture());
        final ExpressionContainerGrid expressionContainer = (ExpressionContainerGrid) expressionContainerArgumentCaptor.getValue();
        assertFalse(expressionContainer.isOnlyVisualChangeAllowed());
    }

    @Test
    public void testSetExpressionWhenOnlyVisualChangeAllowed() {
        final Optional<HasName> hasName = Optional.empty();

        view.setExpression(NODE_UUID,
                           hasExpression,
                           hasName,
                           true);

        verify(gridLayer).add(expressionContainerArgumentCaptor.capture());
        final ExpressionContainerGrid expressionContainer = (ExpressionContainerGrid) expressionContainerArgumentCaptor.getValue();
        assertTrue(expressionContainer.isOnlyVisualChangeAllowed());
    }

    @Test
    public void testSetExpressionDoesUpdateExpressionNameTextWhenHasNameIsNotEmpty() {
        final String NAME = "NAME";
        final Name name = new Name(NAME);
        final HasName hasNameMock = mock(HasName.class);
        doReturn(name).when(hasNameMock).getName();
        final Optional<HasName> hasName = Optional.of(hasNameMock);

        view.setExpression(NODE_UUID,
                           hasExpression,
                           hasName,
                           false);

        verify(expressionName).setTextContent(eq(NAME));
    }

    @Test
    public void testSetExpressionDoesNotUpdateExpressionNameTextWhenHasNameIsEmpty() {
        final Optional<HasName> hasName = Optional.empty();

        view.setExpression(NODE_UUID,
                           hasExpression,
                           hasName,
                           false);

        verify(expressionName, never()).setTextContent(any(String.class));
    }

    @Test
    public void testSetExpressionDoesUpdateExpressionTypeTextWhenHasExpressionIsNotEmpty() {
        final Expression expression = new LiteralExpression();
        final Optional<HasName> hasName = Optional.empty();
        when(hasExpression.getExpression()).thenReturn(expression);

        view.setExpression(NODE_UUID,
                           hasExpression,
                           hasName,
                           false);

        verify(expressionType).setTextContent(eq(LITERAL_EXPRESSION.getText()));
    }

    @Test
    public void testSetExpressionDoesNotUpdateExpressionTypeTextWhenHasExpressionTextWhenHasExpressionIsEmpty() {
        final Optional<HasName> hasName = Optional.empty();

        view.setExpression(NODE_UUID,
                           hasExpression,
                           hasName,
                           false);

        verify(expressionType).setTextContent(eq("<" + UNDEFINED_EXPRESSION_DEFINITION_NAME + ">"));
    }

    @Test
    public void testOnClickReturnToLink() {
        view.onClickReturnToLink(mock(ClickEvent.class));

        verify(presenter).exit();
    }

    @Test
    public void testRefresh() {
        view.refresh();

        verify(gridLayer).batch();
    }

    @Test
    public void testSetFocus() {
        view.setFocus();

        verify(gridPanel).setFocus(true);
    }

    @Test
    public void testOnTryIt() {
        final ClickEvent event = mock(ClickEvent.class);
        view.setExpression(NODE_UUID,
                           hasExpression,
                           Optional.of(HasName.NOP),
                           false);

        view.onTryIt(event);

        verify(view).renderNewBoxedExpression();
        verify(view).toggleBoxedExpression(true);
        verify(event).preventDefault();
        verify(event).stopPropagation();
    }

    @Test
    public void testOnSwitchBack() {
        final ClickEvent event = mock(ClickEvent.class);
        view.setExpression(NODE_UUID,
                           hasExpression,
                           Optional.of(HasName.NOP),
                           false);

        view.onSwitchBack(event);

        verify(view).renderOldBoxedExpression();
        verify(view).toggleBoxedExpression(false);
        verify(event).preventDefault();
        verify(event).stopPropagation();
    }

    @Test
    public void testToggleBoxedExpressionAndEnableIt() {
        view.toggleBoxedExpression(true);
        verify(betaBoxedExpressionToggle.classList).toggle(ENABLED_BETA_CSS_CLASS, true);
    }

    @Test
    public void testToggleBoxedExpressionAndDisableIt() {
        view.toggleBoxedExpression(false);
        verify(betaBoxedExpressionToggle.classList).toggle(ENABLED_BETA_CSS_CLASS, false);
    }

    @Test
    public void testClear() {

        final ExpressionContainerGrid expressionContainerGrid = mock(ExpressionContainerGrid.class);

        doReturn(expressionContainerGrid).when(view).getExpressionContainerGrid();

        view.clear();

        verify(expressionContainerGrid).clearExpressionType();
    }

    @Test
    public void testResetExpressionDefinition() {

        final ExpressionProps props = new ExpressionProps(NAME,
                                                          "datatype",
                                                          "logicType");

        view.resetExpressionDefinition(props);

        verify(sessionCommandManager).execute(eq(canvasHandler),
                                              commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof ClearExpressionCommand);
        assertCommandParameters(command, props);
    }

    private void assertCommandParameters(final FillExpressionCommand command,
                                         final ExpressionProps props) {
        assertEquals(hasExpression, command.getHasExpression());
        assertEquals(props, command.getExpressionProps());
        assertEquals(editorSelectedEvent, command.getEditorSelectedEvent());
        assertEquals(NODE_UUID, command.getNodeUUID());
    }

    @Test
    public void testBroadcastLiteralExpressionDefinition() {

        final LiteralProps props = mock(LiteralProps.class);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastLiteralExpressionDefinition(props);

        verify(view).executeIfItHaveChanges(commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof FillLiteralExpressionCommand);
        assertCommandParameters(command, props);
    }

    @Test
    public void testBroadcastContextExpressionDefinition_WhenIsUserAction() {

        final ContextProps props = mock(ContextProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(true);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastContextExpressionDefinition(props);

        verify(view).executeIfItHaveChanges(commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof FillContextExpressionCommand);
        assertCommandParameters(command, props);
    }

    @Test
    public void testBroadcastContextExpressionDefinition_WhenIsNotUserAction() {

        final ContextProps props = mock(ContextProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(false);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastContextExpressionDefinition(props);

        verify(view, never()).executeIfItHaveChanges(any());
    }

    @Test
    public void testBroadcastContextExpressionDefinition_WhenIsValidProps() {

        final RelationProps props = mock(RelationProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(true);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastRelationExpressionDefinition(props);

        verify(view).executeIfItHaveChanges(commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof FillRelationExpressionCommand);
        assertCommandParameters(command, props);
    }

    @Test
    public void testBroadcastContextExpressionDefinition_WhenItIsNotUserAction() {

        final RelationProps props = mock(RelationProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(false);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastRelationExpressionDefinition(props);

        verify(view, never()).executeIfItHaveChanges(any());
    }

    @Test
    public void testBroadcastListExpressionDefinition() {

        final ListProps props = mock(ListProps.class);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastListExpressionDefinition(props);

        verify(view).executeIfItHaveChanges(commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof FillListExpressionCommand);
        assertCommandParameters(command, props);
    }

    @Test
    public void testBroadcastInvocationExpressionDefinition_WhenItIsUserAction() {

        final InvocationProps props = mock(InvocationProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(true);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastInvocationExpressionDefinition(props);

        verify(view).executeIfItHaveChanges(commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof FillInvocationExpressionCommand);
        assertCommandParameters(command, props);
    }

    @Test
    public void testBroadcastInvocationExpressionDefinition_WhenItIsNotUserAction() {

        final InvocationProps props = mock(InvocationProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(false);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastInvocationExpressionDefinition(props);

        verify(view, never()).executeIfItHaveChanges(any());
    }

    @Test
    public void testBroadcastFunctionExpressionDefinition() {

        final FunctionProps props = mock(FunctionProps.class);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastFunctionExpressionDefinition(props);

        verify(view).executeIfItHaveChanges(commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof FillFunctionExpressionCommand);
        assertCommandParameters(command, props);
    }

    @Test
    public void testBroadcastDecisionTableExpressionDefinition_WhenIsUserAction() {

        final DecisionTableProps props = mock(DecisionTableProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(true);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastDecisionTableExpressionDefinition(props);

        verify(view).executeIfItHaveChanges(commandCaptor.capture());

        final FillExpressionCommand command = commandCaptor.getValue();

        assertTrue(command instanceof FillDecisionTableExpressionCommand);
        assertCommandParameters(command, props);
    }

    @Test
    public void testBroadcastDecisionTableExpressionDefinition_WhenIsNotUserAction() {

        final ContextProps props = mock(ContextProps.class);

        when(userActionChecker.isUserAction(props)).thenReturn(false);

        doNothing().when(view).executeIfItHaveChanges(any());

        view.broadcastContextExpressionDefinition(props);

        verify(view, never()).executeIfItHaveChanges(any());
    }

    @Test
    public void testExecuteIfItHaveChanges() {

        final FillExpressionCommand command = mock(FillExpressionCommand.class);
        final CompositeCommand.Builder commandBuilder = mock(CompositeCommand.Builder.class);

        when(command.hasChanges()).thenReturn(true);

        doNothing().when(view).addExpressionCommand(command, commandBuilder);
        doNothing().when(view).addUpdatePropertyNameCommand(commandBuilder);
        doNothing().when(view).execute(commandBuilder);
        doReturn(commandBuilder).when(view).createCommandBuilder();

        final InOrder inOrder = Mockito.inOrder(view);

        view.executeIfItHaveChanges(command);

        inOrder.verify(view).createCommandBuilder();
        inOrder.verify(view).addExpressionCommand(command, commandBuilder);
        inOrder.verify(view).addUpdatePropertyNameCommand(commandBuilder);
        inOrder.verify(view).execute(commandBuilder);
    }

    @Test
    public void testExecuteIfItHaveChanges_WhenThereIsNot() {

        final FillExpressionCommand command = mock(FillExpressionCommand.class);
        final CompositeCommand.Builder commandBuilder = mock(CompositeCommand.Builder.class);

        when(command.hasChanges()).thenReturn(false);

        view.executeIfItHaveChanges(command);

        verify(view, never()).createCommandBuilder();
        verify(view, never()).addExpressionCommand(command, commandBuilder);
        verify(view, never()).addUpdatePropertyNameCommand(commandBuilder);
        verify(view, never()).execute(commandBuilder);
    }

    @Test
    public void testExecute() {

        final CompositeCommand.Builder commandBuilder = mock(CompositeCommand.Builder.class);
        final CompositeCommand compositeCommand = mock(CompositeCommand.class);

        when(commandBuilder.build()).thenReturn(compositeCommand);

        view.execute(commandBuilder);

        verify(sessionCommandManager).execute(canvasHandler, compositeCommand);
    }

    @Test
    public void testAddUpdatePropertyNameCommand() {

        final CompositeCommand.Builder commandBuilder = mock(CompositeCommand.Builder.class);
        final Index graphIndex = mock(Index.class);
        final org.kie.workbench.common.stunner.core.graph.Element element = mock(org.kie.workbench.common.stunner.core.graph.Element.class);
        final Definition definition = mock(Definition.class);
        final Object theDefinition = mock(Object.class);
        final HasName hasName = mock(HasName.class);
        final Optional<HasName> optionalHasName = Optional.of(hasName);
        final Name name = mock(Name.class);
        final String nameId = "nameId";
        final CanvasCommand<AbstractCanvasHandler> updateCommand = mock(CanvasCommand.class);

        doReturn(optionalHasName).when(view).getHasName();

        when(hasName.getValue()).thenReturn(name);
        when(definition.getDefinition()).thenReturn(theDefinition);
        when(canvasCommandFactory.updatePropertyValue(element, nameId, name)).thenReturn(updateCommand);
        when(definitionUtils.getNameIdentifier(theDefinition)).thenReturn(nameId);
        when(element.getContent()).thenReturn(definition);
        when(graphIndex.get(NODE_UUID)).thenReturn(element);
        when(canvasHandler.getGraphIndex()).thenReturn(graphIndex);

        view.addUpdatePropertyNameCommand(commandBuilder);

        verify(commandBuilder).addCommand(updateCommand);
    }

    @Test
    public void testAddExpressionCommand() {

        final FillExpressionCommand expressionCommand = mock(FillExpressionCommand.class);
        final CompositeCommand.Builder builder = mock(CompositeCommand.Builder.class);

        view.addExpressionCommand(expressionCommand, builder);

        verify(builder).addCommand(expressionCommand);
    }
}
