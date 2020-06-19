package i5.las2peer.services.learningLockerService;

import java.util.List;

public class NewClient {

    private String name;
    private String lrs_id;
    private String title;
    private List<String> scopes;

    public NewClient(String name, String lrs_id, String title, List<String> scopes) {

        this.name = name;
        this.title = title;
        this.lrs_id = lrs_id;
        this.scopes = scopes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLrs_id() {
        return lrs_id;
    }

    public void setLrs_id(String lrs_id) {
        this.lrs_id = lrs_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}
