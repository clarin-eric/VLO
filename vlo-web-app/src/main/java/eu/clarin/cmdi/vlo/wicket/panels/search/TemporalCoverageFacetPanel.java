/*
 * Copyright (C) 2016 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket.panels.search;

import com.googlecode.wicket.jquery.ui.form.slider.AjaxRangeSlider;
import com.googlecode.wicket.jquery.ui.form.slider.RangeValue;
import com.googlecode.wicket.jquery.ui.panel.JQueryFeedbackPanel;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.TemporalCoverageRange;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.LambdaModel;

/**
 * Temporal coverage range slider panel
 *
 * @author Tariq Yousef
 */
public abstract class TemporalCoverageFacetPanel extends ExpandablePanel<QueryFacetsSelection> implements IAjaxIndicatorAware {

    @SpringBean
    private FieldNameService fieldNameService;

    private final String TEMPORAL_COVERAGE = fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE);
    private final IModel<FacetSelectionType> selectionTypeModeModel;
    private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();
    private final IModel<TemporalCoverageRange> temporalCoverageRangeModel;
    private final AjaxRangeSlider slider;

    private final static Pattern RANGE_PATTERN = Pattern.compile("\\[(\\d+) TO (\\d+)\\]");
    private final static int RANGE_PATTERN_LOWER_GROUP = 1;
    private final static int RANGE_PATTERN_UPPER_GROUP = 2;

    public TemporalCoverageFacetPanel(String id,
            final IModel<QueryFacetsSelection> selectionModel,
            final IModel<FacetSelectionType> selectionTypeModeModel,
            IModel<TemporalCoverageRange> temporalCoverageRangeModel) {
        super(id, selectionModel);
        this.temporalCoverageRangeModel = temporalCoverageRangeModel;
        this.selectionTypeModeModel = selectionTypeModeModel;

        final IModel<RangeValue> rangeModel = temporalCoverageRangeModel.map(tcr -> new RangeValue(tcr.getStart(), tcr.getEnd()));
        final Form<RangeValue> form = new Form<>("temporalCoverage", rangeModel);
        final FeedbackPanel feedback = new JQueryFeedbackPanel("feedbackTemporalCoverage");
        form.add(feedback.setOutputMarkupId(true));

        final IModel<Integer> lowerModel = LambdaModel.of(
                () -> getBoundFromSelection(selectionModel, rangeModel, RANGE_PATTERN_LOWER_GROUP),
                (lower) -> {
                    applySelection(lower, rangeModel.getObject().getUpper());
                }
        );

        final IModel<Integer> upperModel = LambdaModel.of(
                () -> getBoundFromSelection(selectionModel, rangeModel, RANGE_PATTERN_UPPER_GROUP),
                (upper) -> {
                    applySelection(rangeModel.getObject().getLower(), upper);
                }
        );

        final TextField<Integer> lowerInput = new TextField<>("lower", lowerModel);
        lowerInput.setType(Integer.class);
        final TextField<Integer> upperInput = new TextField<>("upper", upperModel);
        upperInput.setType(Integer.class);

        slider = new AjaxRangeSlider("slider", rangeModel, lowerInput, upperInput) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onError(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(feedback); // do never add 'this' or the form here!
                }
            }

            @Override
            public void onValueChanged(IPartialPageRequestHandler handler) {
                if (handler instanceof AjaxRequestTarget) {
                    selectionChanged(Optional.of((AjaxRequestTarget) handler));
                } else {
                    selectionChanged(Optional.empty());
                }
            }
        };
        form.add(lowerInput);
        form.add(upperInput);
        form.add(slider);

        form.add(indicatorAppender);
        add(form);
    }

    private void applySelection(int lower, int upper) {
        final String sel = "[" + lower + " TO " + upper + "]";
        getModelObject().addSingleFacetValue(TEMPORAL_COVERAGE, selectionTypeModeModel.getObject(), Collections.singleton(sel));
    }

    private Integer getBoundFromSelection(final IModel<QueryFacetsSelection> selectionModel, final IModel<RangeValue> rangeModel, final int rangeMatchGroup) {
        Optional<String> coverageSelection = Optional.ofNullable(selectionModel.getObject())
                .flatMap(selection -> Optional.ofNullable(selection.getSelectionValues(TEMPORAL_COVERAGE)))
                .map(FacetSelection::getValues)
                .flatMap(values -> values.size() == 1 ? Optional.of(values.iterator().next()) : Optional.empty());

        return coverageSelection.flatMap(
                s -> {
                    final Matcher matcher = RANGE_PATTERN.matcher(s);
                    if (matcher.matches()) {
                        try {
                            return Optional
                                    .ofNullable(matcher.group(rangeMatchGroup))
                                    .map(Integer::parseInt);
                        } catch (NumberFormatException ex) {
                            //TODO log
                        }
                    }
                    return Optional.empty();
                }
        ).orElse(rangeModel.getObject().getLower());
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        if (getModelObject().getSelectionValues(TEMPORAL_COVERAGE) != null) {
            //if there any selection, make initially expanded
            getExpansionModel().setObject(ExpansionState.EXPANDED);
        }

        slider
                .setMin(temporalCoverageRangeModel.getObject().getStart()).setMax(temporalCoverageRangeModel.getObject().getEnd())
                .setRangeValidator(new RangeValidator<>(temporalCoverageRangeModel.getObject().getStart(), temporalCoverageRangeModel.getObject().getEnd()));

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, new StringResourceModel("temporalCoverageTitle"));
    }

    @Override
    public void detachModels() {
        super.detachModels();

        if (this.temporalCoverageRangeModel != null) {
            this.temporalCoverageRangeModel.detach();
        }

        if (selectionTypeModeModel != null) {
            this.selectionTypeModeModel.detach();
        }
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return indicatorAppender.getMarkupId();
    }
    
    protected abstract void selectionChanged(Optional<AjaxRequestTarget> target);

}
