def Container-> join(splitSign)
    var str = ""
    var keys = this.keys
    splitSign ||= ", "
    for var i = 0; i < keys.length; i += 1 do
        var key = keys[i]
        var value = this[key]
        str += key
        str += " : "
        str += value
        str += ", " when i < keys.length - 1
    end
    return str
end

def Container-> toString
    return "{" + this.join + "}"
end