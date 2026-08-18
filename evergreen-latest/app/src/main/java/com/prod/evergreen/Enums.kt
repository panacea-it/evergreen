package com.prod.evergreen

class Enums {

    companion object {
        enum class ClientRole {
            eg_super_admin,
            technician,
            eg_admin,
            client,
            client_admin,
            others
        }
    }
}