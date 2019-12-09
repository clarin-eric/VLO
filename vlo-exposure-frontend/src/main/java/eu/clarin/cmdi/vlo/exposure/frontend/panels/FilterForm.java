package eu.clarin.cmdi.vlo.exposure.frontend.panels;

import eu.clarin.cmdi.vlo.exposure.frontend.HomePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.sql.Timestamp;

public class FilterForm extends Form<Filter> {
    private static final String WICKET_ID_WILDCARD = "recordIdWC";
    private static final String WICKET_ID_STARTDATE = "startDate";
    private static final String WICKET_ID_ENDDATE = "endDate";
    private Filter filter;
    private TextField recordIdwc;
    private TextField sDate;
    private TextField eDate;

    public FilterForm(String id,  IModel<Filter> filter) {
        super(id, new CompoundPropertyModel(filter));
        this.filter = filter.getObject();
        init();
    }
    private void init() {
        recordIdwc = new TextField(WICKET_ID_WILDCARD, new PropertyModel(filter, "recordIdWildCard"));
        sDate = new TextField(WICKET_ID_STARTDATE, new PropertyModel(filter, "startDate"));
        eDate = new TextField(WICKET_ID_ENDDATE, new PropertyModel(filter, "endDate"));
        add(recordIdwc);
        add(sDate);
        add(eDate);

    }

    @Override
    protected void onSubmit() {
        String wc, sDate, eDate;
        try{
             sDate = this.sDate.getModelObject().toString();
        }catch (Exception e){
            sDate="";
        }
        try{
             eDate = this.eDate.getModelObject().toString();
        }catch (Exception e){
            eDate="";
        }
        try{
            wc = recordIdwc.getModelObject().toString();
        }catch (Exception e){
            wc="%";
        }
        PageParameters params = new PageParameters();
        params.add("startDate", sDate);
        params.add("endDate", eDate);
        params.add("recordIdWC", wc);
        this.setResponsePage(HomePage.class, params);


    }
}
