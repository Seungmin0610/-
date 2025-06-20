package IdealTypeWorldCup;

public class Person {
    private String name;
    private String imagePath;
    private String description;

    public Person(String name, String imagePath, String description) {
        this.name = name;
        this.imagePath = imagePath;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDescription() {
        return description;
    }
}