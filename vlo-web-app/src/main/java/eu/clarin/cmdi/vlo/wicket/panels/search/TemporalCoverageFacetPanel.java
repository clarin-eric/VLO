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

import eu.clarin.cmdi.vlo.wicket.model.UnwrappedOptionalModel;
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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporal coverage range slider panel
 *
 * @author Tariq Yousef
 * @author Twan Goosen
 */
public abstract class TemporalCoverageFacetPanel extends ExpandablePanel<QueryFacetsSelection> implements IAjaxIndicatorAware {

    private final static Logger logger = LoggerFactory.getLogger(TemporalCoverageFacetPanel.class);

    @SpringBean
    private FieldNameService fieldNameService;

    private final String TEMPORAL_COVERAGE = fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE);
    private final IModel<FacetSelectionType> selectionTypeModeModel;
    private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();
    private final IModel<TemporalCoverageRange> temporalCoverageRangeModel;

    private final IModel<Boolean> filterEnabledModel;

    private final static Pattern RANGE_PATTERN = Pattern.compile("\\[(\\d+) TO (\\d+)\\]");
    private final static int RANGE_PATTERN_LOWER_GROUP = 1;
    private final static int RANGE_PATTERN_UPPER_GROUP = 2;

    private IModel<Optional<Integer>> upperModel;
    private IModel<Optional<Integer>> lowerModel;
    private final IModel<RangeValue> rangeModel;

    public TemporalCoverageFacetPanel(String id,
            final IModel<QueryFacetsSelection> selectionModel,
            final IModel<FacetSelectionType> selectionTypeModeModel,
            IModel<TemporalCoverageRange> temporalCoverageRangeModel) {
        super(id, selectionModel);
        this.temporalCoverageRangeModel = temporalCoverageRangeModel;
        this.selectionTypeModeModel = selectionTypeModeModel;
        this.filterEnabledModel = new Model(false);

        lowerModel = LambdaModel.of(
                () -> getBoundFromSelection(selectionModel, RANGE_PATTERN_LOWER_GROUP, () -> temporalCoverageRangeModel.getObject().getStart()),
                (lower) -> {
                    applySelection(lower, upperModel.getObject());
                }
        );

        upperModel = LambdaModel.of(
                () -> getBoundFromSelection(selectionModel, RANGE_PATTERN_UPPER_GROUP, () -> temporalCoverageRangeModel.getObject().getEnd()),
                (upper) -> {
                    applySelection(lowerModel.getObject(), upper);
                }
        );

        rangeModel = new RangeValueModel(lowerModel, upperModel);

        final Form<RangeValue> form = createTemporalCoverageForm("temporalCoverage", temporalCoverageRangeModel);
        add(form);
    }

    private Form<RangeValue> createTemporalCoverageForm(String id, IModel<TemporalCoverageRange> temporalCoverageRangeModel1) {
        final Form<RangeValue> form = new Form<>(id, rangeModel);

        // Form has its own feedback panel
        final FeedbackPanel feedback = new JQueryFeedbackPanel("feedbackTemporalCoverage");
        form.add(feedback.setOutputMarkupId(true));

        // Inputs to determine range: text boxes for upper/lower bound
        final TextField<Integer> lowerInput = new TextField<>("lower", new UnwrappedOptionalModel<>(lowerModel));
        lowerInput.setType(Integer.class);
        form.add(lowerInput);

        final TextField<Integer> upperInput = new TextField<>("upper", new UnwrappedOptionalModel<>(upperModel));
        upperInput.setType(Integer.class);
        form.add(upperInput);

        // Inputs to determine range: slider
        final TemporalCoverageAjaxRangeSlider slider = new TemporalCoverageAjaxRangeSlider("slider", rangeModel, lowerInput, upperInput, feedback);
        form.add(slider);

        // Checkbox to enable/disable filter
        final AjaxCheckBox enabledToggle = new AjaxCheckBox("filterEnabled", filterEnabledModel) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                applySelection(lowerModel.getObject(), upperModel.getObject());
                selectionChanged(Optional.ofNullable(target));
                if (target != null) {
                    target.add(form);
                }
            }
        };
        form.add(enabledToggle);

        // Make submit button an Ajax enabled one to prevent page reload on submit
        form.add(new AjaxFallbackButton("submit", form) {
            @Override
            protected void onSubmit(Optional<AjaxRequestTarget> target) {
                selectionChanged(target);
                target.ifPresent(t -> {
                    t.add(form);
                });
            }

        });

        // Synchronise the form controls with the models
        form.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                final Optional<Integer> start = temporalCoverageRangeModel1.getObject().getStart();
                final Optional<Integer> end = temporalCoverageRangeModel1.getObject().getEnd();
                slider
                        .setDisabled(!filterEnabledModel.getObject())
                        .setMin(start.orElse(null))
                        .setMax(end.orElse(null))
                        .setRangeValidator(new RangeValidator<>(start.orElse(-10000), end.orElse(10000)));
                lowerInput.setEnabled(filterEnabledModel.getObject());
                upperInput.setEnabled(filterEnabledModel.getObject());
                enabledToggle.setEnabled(!start.isEmpty() || !end.isEmpty());
            }
        });

        // Show spinner while busy...
        form.add(indicatorAppender);

        return form;
    }

    private void applySelection(Optional<Integer> lower, Optional<Integer> upper) {
        if (filterEnabledModel.getObject()) {
            final String sel = "[" + lower.map(Objects::toString).orElse("*") + " TO " + upper.map(Objects::toString).orElse("*") + "]";
            getModelObject().addSingleFacetValue(TEMPORAL_COVERAGE, selectionTypeModeModel.getObject(), Collections.singleton(sel));
        } else {
            getModelObject().removeFacetSelection(TEMPORAL_COVERAGE);
        }
    }

    private Optional<Integer> getBoundFromSelection(final IModel<QueryFacetsSelection> selectionModel, final int rangeMatchGroup, Supplier<Optional<Integer>> defaultProvider) {
        Optional<String> coverageSelection = Optional.ofNullable(selectionModel.getObject())
                .flatMap(selection -> Optional.ofNullable(selection.getSelectionValues(TEMPORAL_COVERAGE)))
                .map(FacetSelection::getValues)
                .flatMap(values -> values.size() == 1 ? Optional.of(values.iterator().next()) : Optional.empty());

        if (coverageSelection.isEmpty()) {
            return defaultProvider.get();
        } else {
            return coverageSelection.flatMap(
                    s -> {
                        final Matcher matcher = RANGE_PATTERN.matcher(s);
                        if (matcher.matches()) {
                            try {
                                return Optional
                                        .ofNullable(matcher.group(rangeMatchGroup))
                                        .map(Integer::parseInt);
                            } catch (NumberFormatException ex) {
                                logger.debug("User entered invalid value for date range", ex);
                            }
                        }
                        return defaultProvider.get();
                    }
            );
        }
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final QueryFacetsSelection modelObject = getModelObject();
        if (modelObject != null) {
            final Map<String, FacetSelection> selection = modelObject.getSelection();

            if (!filterEnabledModel.getObject() && selection != null) {
                // enable temporal coverage control if temporal coverage filter is set
                filterEnabledModel.setObject(selection.containsKey(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE)));
            }

            if (modelObject.getSelectionValues(TEMPORAL_COVERAGE) != null) {
                //if there is any temporal coverage selection, make initially expanded
                getExpansionModel().setObject(ExpansionState.EXPANDED);
            }
        }
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, new StringResourceModel("temporalCoverageTitle"));
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return indicatorAppender.getMarkupId();
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

        upperModel.detach();
        lowerModel.detach();
        rangeModel.detach();
    }

    protected abstract void selectionChanged(Optional<AjaxRequestTarget> target);

    private static class RangeValueModel extends LoadableDetachableModel<RangeValue> {

        private final IModel<Optional<Integer>> lowerModel;
        private final IModel<Optional<Integer>> upperModel;

        public RangeValueModel(IModel<Optional<Integer>> lowerModel, IModel<Optional<Integer>> upperModel) {
            this.lowerModel = lowerModel;
            this.upperModel = upperModel;
        }

        @Override
        protected RangeValue load() {
            return new RangeValue(
                    lowerModel.orElse(Optional.empty()).getObject().orElse(null),
                    upperModel.orElse(Optional.empty()).getObject().orElse(null));
        }

        @Override
        public void setObject(RangeValue object) {
            super.setObject(object);
            lowerModel.setObject(Optional.of(object.getLower()));
            upperModel.setObject(Optional.of(object.getUpper()));
        }

    }

    private class TemporalCoverageAjaxRangeSlider extends AjaxRangeSlider {

        private final FeedbackPanel feedback;

        public TemporalCoverageAjaxRangeSlider(String id, IModel<RangeValue> model, TextField<Integer> lower, TextField<Integer> upper, FeedbackPanel feedback) {
            super(id, model, lower, upper);
            this.feedback = feedback;
        }
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
                final AjaxRequestTarget target = (AjaxRequestTarget) handler;
                selectionChanged(Optional.of(target));
            } else {
                selectionChanged(Optional.empty());
            }
        }

        public TemporalCoverageAjaxRangeSlider setDisabled(boolean disabled) {
            options.set("disabled", disabled);
            return this;
        }
    }

}
