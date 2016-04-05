package la.oja.senseware.models;

/**
 * Created by Mariale on 21-12-2015.
 */
public class Days {

    private  int id_day;
    private int day;
    private String title;
    private int visibleclasses;
    private int visible;

    public Days(){
    }

    public Days(int id_day, int day, String title, int visibleClasses, int visible){
        this.id_day = id_day;
        this.day = day;
        this.title = title;
        this.visibleclasses = visibleClasses;
        this.visible = visible;

    }

    public int getId_day()
    {
        return this.id_day;
    }

    public int getDay() {
        return this.day;
    }

    public String getTitle() {
        return this.title;
    }

    public int getVisibleclasses() {
        return this.visibleclasses;
    }

    public int getVisible()
    {
        return this.visible;
    }

}
