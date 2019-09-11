/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.service.UriResolver;
import eu.clarin.cmdi.vlo.wicket.model.PIDContext;
import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.PIDTypeModel;
import java.util.Optional;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDInfoPanel extends GenericPanel<String> {

    @SpringBean
    private UriResolver uriResolver;

    private final IModel<PIDContext> pidContextModel;

    public PIDInfoPanel(String id, IModel<String> model, IModel<PIDContext> pidContextModel) {
        super(id, PIDLinkModel.wrapLinkModel(model));
        this.pidContextModel = pidContextModel;
        setOutputMarkupId(true);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final IModel<String> pidLinkModel = getModel();
        final PIDTypeModel pidTypeModel = new PIDTypeModel(pidLinkModel);

        add(new TextField("pidInputField", pidLinkModel));

        final StringResourceModel pidContextLabelModel = new StringResourceModel("pidContext.${}", this, pidContextModel);
        add(new Label("pidContextLabel1", pidContextLabelModel));
        add(new Label("pidContextLabel2", pidContextLabelModel));

        add(new ExternalLink("pidLink", pidLinkModel)
                .add(new Label("pidContextLabel3", pidContextLabelModel)));

        final StringResourceModel pidTypeLabelModel = new StringResourceModel("pidType.${}", this, pidTypeModel);
        add(new Label("pidTypeLabel", pidTypeLabelModel));

        final StringResourceModel pidTypeLabelPluralModel = new StringResourceModel("pidType.${}.plural", this, pidTypeModel);
        add(new Label("pidTypeLabelPlural", pidTypeLabelPluralModel));

        //container for resolved link
        final WebMarkupContainer resolvedLinkPanel = new WebMarkupContainer("resolvedLinkPanel");
        //lazy panel for async resolving (link is not added initially)
        final AjaxLazyLoadPanel lazyResolvedUrlLink = new AjaxLazyLoadPanel<ResolvedPidLink>("resolvedLink") {
            @Override
            public ResolvedPidLink getLazyLoadComponent(String markupId) {
                return new ResolvedPidLink(markupId, pidLinkModel);
            }

            @Override
            protected void onContentLoaded(ResolvedPidLink content, Optional<AjaxRequestTarget> target) {
                super.onContentLoaded(content, target);
                target.ifPresent(t -> {
                    t.add(resolvedLinkPanel);
                });
            }
        };

        add(resolvedLinkPanel
                .add(lazyResolvedUrlLink)
                //has to be ajax updateable
                .setOutputMarkupId(true)
                //only visible if PID can be pre-resolved
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(uriResolver.canResolve(pidLinkModel.getObject()));
                    }

                })
        );
    }

}
