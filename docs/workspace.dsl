workspace "Name" "Description" {

    !identifiers hierarchical

    model {

        user = person "User"

        system = softwareSystem "Metronome" {

            mobileApp = container "Mobile app" {
                metronome = component "Metronome" "Aggregate providing external API. Sets up lifecycle of composing components"

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


                metronome -> conductor "listens to rhythmic intervals"
                metronome -> player "instructs player to play sounds"

                app = component "App" {
                    description "Parent component that handles supervises child components lifecycle, errors"
                }

                session = component "Session" {
                    description "Child component that manages metronome sessions"
                    technology "Dagger, Coroutines"
                }
                session -> app "Sends errors via CoroutineScope hierarchy" {
                    tags "error"
                }
                session -> metronome "Creates and provides to clients (e.g. UI)"
                app -> session "Start, stops, restarts in case error occurs"

            }
            user -> mobileApp "Practices rhythm with"
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
            relationship "error" {
                color #ff0000
                dashed true
                thickness 2
            }
        }
    }

    configuration {
        scope softwaresystem
    }

}