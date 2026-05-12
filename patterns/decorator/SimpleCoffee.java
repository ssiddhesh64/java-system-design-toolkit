package Design.patterns.decorator;

interface Coffee {

    String description();

    int cost();
}

public class SimpleCoffee implements Coffee {

    @Override
    public String description() {
        return "Simple Coffee";
    }

    @Override
    public int cost() {
        return 100;
    }

    public static void main(String[] args) {

        Coffee coffee = new SugarDecorator(new MilkDecorator(new SimpleCoffee()));

        System.out.println("Coffee details");
        System.out.println(coffee.description() + " " + coffee.cost());
    }
}

abstract class CoffeeDecorator implements Coffee{

    protected Coffee coffee;

    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
}

class MilkDecorator extends CoffeeDecorator {

    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String description() {
        return coffee.description() + " ,Milk";
    }

    @Override
    public int cost() {
        return coffee.cost() + 20;
    }
}

class SugarDecorator extends CoffeeDecorator {

    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String description() {
        return coffee.description() + " ,Sugar";
    }

    @Override
    public int cost() {
        return coffee.cost() + 10;
    }
}