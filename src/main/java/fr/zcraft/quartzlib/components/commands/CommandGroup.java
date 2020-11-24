package fr.zcraft.quartzlib.components.commands;

import fr.zcraft.quartzlib.components.commands.exceptions.CommandException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class CommandGroup extends CommandNode {
    private final Class<?> commandGroupClass;
    private final Supplier<?> classInstanceSupplier;

    private final Map<String, CommandNode> subCommands = new HashMap<>();

    public CommandGroup(Class<?> commandGroupClass, Supplier<?> classInstanceSupplier, String name, ArgumentTypeHandlerCollection typeHandlerCollection) {
        this(commandGroupClass, classInstanceSupplier, name, typeHandlerCollection, null);
    }

    public CommandGroup(Class<?> commandGroupClass, Supplier<?> classInstanceSupplier, String name, ArgumentTypeHandlerCollection typeHandlerCollection, CommandGroup parent) {
        super(name, parent);
        this.commandGroupClass = commandGroupClass;
        this.classInstanceSupplier = classInstanceSupplier;
        DiscoveryUtils.getCommandMethods(commandGroupClass, typeHandlerCollection).forEach(this::addMethod);
    }

    public Iterable<CommandNode> getSubCommands () {
        return this.subCommands.values();
    }


    private void addMethod(CommandMethod method) {
        // TODO: handle adding to non-endpoints
        CommandEndpoint endpoint = (CommandEndpoint) subCommands.get(method.getName());
        if (endpoint == null) {
            endpoint = new CommandEndpoint(method.getName());
            subCommands.put(endpoint.getName(), endpoint);
        }
        endpoint.addMethod(method);
    }

    void run(String... args) throws CommandException {
        Object commandObject = classInstanceSupplier.get();
        run(commandObject, args);
    }

    @Override
    void run(Object instance, String[] args) throws CommandException {
        String commandName = args[0];
        CommandNode subCommand = subCommands.get(commandName);
        subCommand.run(instance, Arrays.copyOfRange(args, 1, args.length));
    }
}
