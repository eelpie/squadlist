package uk.co.squadlist.web.urls

class SeoLinkBuilder {

    fun makeSeoLinkFor(name: String): String {
        var result = name.toLowerCase().trim { it <= ' ' }.replace("\\s".toRegex(), "-")
        result = result.replace("[^\\-a-z0-9_]".toRegex(), "")
        result = result.replace("--+".toRegex(), "-")
        return result
    }

}
