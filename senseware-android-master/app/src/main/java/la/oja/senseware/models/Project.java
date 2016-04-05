package la.oja.senseware.models;

/**
 * Created by Administrador on 27-11-2015.
 */
public class Project {

    private int id_project;
    private int id_user;
    private String na_project;
    private String create;
    private int id_tmp;

    public Project(){}

    public Project(int id_project, int id_user, String na_project, String create){
        this.id_project = id_project;
        this.id_user = id_user;
        this.na_project = na_project;
        this.create = create;
    }

    public int getId_project() {
        return this.id_project;
    }

    public int getId_user() {
        return this.id_user;
    }

    public String getNa_project(){
        return this.na_project;
    }

    public String getCreate(){

        return this.create;
    }


    public int getId_tmp(){
        return this.id_tmp;
    }

    public void setId_project(int id_project) {
        this.id_project = id_project;
        return;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
        return;
    }

    public void setNa_project(String na_project){
        this.na_project = na_project;
        return;
    }

    public void setCreate(String create){
        this.create = create;
        return;
    }

    public void setId_tmp(int id_tmp){
        this.id_tmp = id_tmp;
        return;
    }


}
