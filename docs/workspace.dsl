workspace "Name" "Description" {

    !identifiers hierarchical

    model {

        u = person "User"

        ss = softwareSystem "Metronome" {

            app = container "App" {
                metronome = component "Metronome" "Aggregate providing external API. Sets up lifecycle of composing components."

                conductor = component "Conductor" "Generates rhythmic intervals"
                conductorSettings = component "Conductor settings" "Manages rhythm settings" {
                    tag "Storage"
                }
                conductor -> conductorSettings "includes"

                player = component "Player" "Deals with sound"
                playerSettings = component "Player settings" "Manages sound track settings" {
                    tag "Storage"
                }
                player -> playerSettings "includes"

                u -> app "Practices rhythm with"
                metronome -> conductor "listens to rhythmic intervals"
                metronome -> player "instructs player to play sounds"
            }

        }
    }

    views {

        styles {
            element "Element" {
                color #ffffff
            }
            element "Person" {
                background #048c04
                shape person
            }
            element "Software System" {
                background #047804
            }
            element "Container" {
                background #55aa55
            }
            element "Storage" {
                shape cylinder
            }
        }
    }

    configuration {
        scope softwaresystem
    }

}