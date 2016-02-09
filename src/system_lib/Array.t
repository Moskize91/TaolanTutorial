def Array-> join(splitSign)
    var str = ""
    splitSign ||= ", "
    for var i = 0; i < this.length; i+=1 do
        str += this.get(i)
        str += splitSign when i < this.length - 1
    end
    return str
end

def Array-> toString
    return "["+ this.join +"]"
end

def Array-> each
    for var i = 0; i < this.length; i+=1 do
        lambda this[i], i
    end
end