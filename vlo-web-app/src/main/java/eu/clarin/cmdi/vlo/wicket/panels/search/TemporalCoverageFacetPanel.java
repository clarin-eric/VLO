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
        final TextField<Integer> lower = new TextField<>("lower", rangeModel.map(RangeValue::getLower));
        final TextField<Integer> upper = new TextField<>("upper", rangeModel.map(RangeValue::getUpper));
        slider = new AjaxRangeSlider("slider", rangeModel, lower, upper) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedback); // do never add 'this' or the form here!
            }

            @Override
            public void onValueChanged(IPartialPageRequestHandler handler) {
                RangeValue value = rangeModel.getObject();
                String sel = "[" + value.getLower() + " TO " + value.getUpper() + "]";
                selectionModel.getObject().addSingleFacetValue(TEMPORAL_COVERAGE, selectionTypeModeModel.getObject(),
                        Collections.singleton(sel));
                if (handler instanceof AjaxRequestTarget) {
                    selectionChanged(Optional.of((AjaxRequestTarget) handler));
                } else {
                    selectionChanged(Optional.empty());
                }
            }
        };
        form.add(lower);
        form.add(upper);

        form.add(slider);

        //form.add(slider.setMin(MIN_VALUE).setMax(MAX_VALUE).setRangeValidator(new RangeValidator<Integer>(MIN_VALUE, MAX_VALUE)));
        form.add(indicatorAppender);
        add(form);
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
                .setRangeValidator(new RangeValidator<Integer>(temporalCoverageRangeModel.getObject().getStart(), temporalCoverageRangeModel.getObject().getEnd()));

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
