package eu.clarin.cmdi.vlo.exposure.frontend.panels;

import eu.clarin.cmdi.vlo.exposure.frontend.Record;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class FilterFormRecord extends Form<Filter> {
    private static final String WICKET_ID_STARTDATE = "startDate";
    private static final String WICKET_ID_ENDDATE = "endDate";

    private Filter filter;
    private TextField sDate;
    private TextField eDate;

    public FilterFormRecord(String id, IModel<Filter> filter) {
        super(id, new CompoundPropertyModel(filter));
        this.filter = filter.getObject();
        init();
    }
    private void init() {
        sDate = new TextField(WICKET_ID_STARTDATE, new PropertyModel(filter, "startDate"));
        eDate = new TextField(WICKET_ID_ENDDATE, new PropertyModel(filter, "endDate"));
        add(sDate);
        add(eDate);

    }

    @Override
    protected void onSubmit() {
        String sDate, eDate;
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

        PageParameters params = new PageParameters();
        params.add("startDate", sDate);
        params.add("endDate", eDate);
        params.add("id", filter.getRecordIdWildCard());
        this.setResponsePage(Record.class, params);
    }
}
