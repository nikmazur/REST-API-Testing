package springrest;

public class Employee {

    private int id;
    private String name;
    private String title;
    private double age;

    public Employee(int id, String name, String title, double age) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public double getAge() {
        return age;
    }


    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Employee)) {
            return false;
        }

        Employee empl = (Employee) o;

        //Returns true if all 3 strings in both objects are equal, false otherwise
        return empl.id == (id) &&
                empl.name.equals(name) &&
                empl.title.equals(title) &&
                empl.age == (age);
    }

}