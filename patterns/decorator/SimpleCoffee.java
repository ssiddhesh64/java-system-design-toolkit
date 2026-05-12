package Design.patterns.decorator;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

interface Coffee {

    String description();

    int cost();
}

public class SimpleCoffee implements Coffee {

    private static final Logger log = Logger.getLogger(SimpleCoffee.class.getName());

    static {
        log.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();

        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        });

        log.addHandler(handler);
    }

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

        log.info("Coffee details");
        log.info(coffee.description() + " " + coffee.cost());
    }
}

abstract class CoffeeDecorator implements Coffee{

    protected Coffee coffee;

    protected CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
}

class MilkDecorator extends CoffeeDecorator {

    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String description() {
        return coffee.description() + ", Milk";
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
        return coffee.description() + ", Sugar";
    }

    @Override
    public int cost() {
        return coffee.cost() + 10;
    }
}