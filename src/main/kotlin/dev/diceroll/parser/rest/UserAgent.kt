package dev.diceroll.parser.rest

data class Agent(val id: String, val version: String)

fun fromUserAgent(userAgent: String?): Agent? {
    if (userAgent == null) {
        return null
    }
    return parse(userAgent)
}

enum class Agents(val id: String, val agentName: String) {
    CURL("curl", "curl"),
    HTTPIE("httpie", "HTTPie");
}
private val TOOL_REGEX ="([^\\/]*)\\/([^ ]*).*".toRegex()

private fun parse(userAgent: String): Agent? {

    val matchResult = TOOL_REGEX.find(userAgent)
    if (matchResult != null) {
        val name = matchResult.groupValues[1]

        for (agent in Agents.values()) {
            if (agent.agentName == name) {
                return Agent(agent.id, matchResult.groupValues[2])
            }
        }
    }

    return null
}
