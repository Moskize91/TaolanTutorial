def Error-> init(message)
    @message= message
    return this
end

def Error-> message
    return @message
end

def Error-> toString
    return "Error : "+ @message
end