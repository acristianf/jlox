# Lox Interpreter in Java
Language interpreter for a dynamic language written in Java, has variables, functions(first-class) and classes. <br/>
Was written following the ["Crafting Interpreters"](https://craftinginterpreters.com) book to learn how languages work inside-out.

## How to run it?

First build the project with: <br/>
```markdown
mvn clean package
```

Following the build you can run the interpreter or a file: <br/>
```
java -jar ./target/lox-VERSION.jar to run the interpreter
java -jar ./target/lox-VERSION.jar [SOURCE] to run file
```

## jlox language example
```markdown
// Functions and variables
fun printSomething(a) {
    print a;
}

printSomething("Hello, ");

var printSomethingVar = printSomething;
printSomethingVar("World!");

// Defining Classes
class Person {
    Person(name) {
        this.name = name;
    }

    shoutName() {
        print "Hello I'm " + this.name + "!";
    }
}

var personInstance = new Person("Cristian");
personInstance.shoutName();
```
