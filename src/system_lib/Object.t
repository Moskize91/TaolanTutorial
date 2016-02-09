def Object-> init
    return this
end

def Object-> print(message)
    message = message.toString when message instanceof Object
    _print message
end

def Object-> toString
    return "Object"
end