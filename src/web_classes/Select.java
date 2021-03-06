package web_classes;

import jpa.entities.Bar;
import jpa.entities.DrinkItem;
import jpa.entities.MenuEntry;
import jsf_managed_beans.UserManagedBean;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowHandler;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Named("dtSelect")
@SessionScoped
public class Select implements Serializable {

    private Bar bar;

    private List<MenuEntry> menuItems;

    private MenuEntry drink;

    @Inject
    private UserManagedBean userManagedBean;

    public MenuEntry getDrink() {
        return drink;
    }

    public void setDrink(MenuEntry drink) {
        this.drink = drink;
    }

    public Bar getBar()
    {
        return bar;
    }

    public void setBar(Bar bar)
    {
        this.bar = bar;
    }
    public List<MenuEntry> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuEntry> menuItems) {
        this.menuItems = menuItems;
    }

    public void back()
    {
        menuItems = null;
        bar = null;
        drink = null;
    }

    public void onRowSelect() throws IOException {
        menuItems = null;
        JSONArray jsonArray = readJsonFromUrl("http://localhost:8080/Dorst19/resources/menu/" + bar.getId());
        parseJson(jsonArray);
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("http://localhost:8080/Dorst19/menu.xhtml");
    }
    public void onDrinkSelect() throws IOException {

        if(userManagedBean.isLoggedIn())
        {
            final FacesContext facesContext = FacesContext.getCurrentInstance();
            final Application application = facesContext.getApplication();

            // get an instance of the flow to start
            final String flowId = (String) userManagedBean.attemptLogin();
            final FlowHandler flowHandler = application.getFlowHandler();
            final Flow targetFlow = flowHandler.getFlow(facesContext,
                    "", // definingDocumentId (empty if flow is defined in "faces-config.xml")
                    flowId);

            // get the navigation handler and the view ID of the flow
            final ConfigurableNavigationHandler navHandler = (ConfigurableNavigationHandler) application.getNavigationHandler();
            final NavigationCase navCase = navHandler.getNavigationCase(facesContext,
                    null, // fromAction
                    flowId);
            final String toViewId = navCase.getToViewId(facesContext);

            // initialize the flow scope
            flowHandler.transition(facesContext,
                    null, // sourceFlow
                    targetFlow,
                    null, // outboundCallNode
                    toViewId); // toViewId

            final String outcome = toViewId + "?faces-redirect=true";
            navHandler.handleNavigation(facesContext,
                    null, // from action
                    outcome);
        }
        else
        {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect("http://localhost:8080/Dorst19/login.xhtml");
        }
    }
    public JSONArray readJsonFromUrl(String nurl) throws IOException {
        URL url = new URL(nurl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responsecode = conn.getResponseCode();
        if(responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: "+responsecode);
        }
        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String output;
        JSONArray jsonArray = null;
        while ((output = br.readLine()) != null) {
            jsonArray = new JSONArray(output);
        }
        conn.disconnect();
        return jsonArray;
    }
    public void parseJson(JSONArray jsonArray)
    {
        menuItems = new ArrayList<MenuEntry>();
        if(jsonArray.length() > 0)
        {
            int i = 0;
            while(i < jsonArray.length())
            {
                JSONObject objectMenu = (JSONObject) jsonArray.get(i);
                JSONObject jsonItem = (JSONObject) objectMenu.get("item");
                DrinkItem item = new DrinkItem((String) jsonItem.get("name"), ((Double) jsonItem.get("alcoholPercentage")).floatValue(), ((Double) jsonItem.get("volume")).floatValue());
                MenuEntry menuEntry = new MenuEntry(item, ((Double) objectMenu.get("price")).floatValue(), (int) objectMenu.get("stock"));
                menuItems.add(menuEntry);
                i++;
            }
        }
    }

}



