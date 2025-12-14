classDiagram
    %% ---------------------------------------------------------
    %% PACKAGE: MAIN (The Controller & UI)
    %% ---------------------------------------------------------
    namespace main {
        class JavaTycoon {
            -String currentProfile
            -List~Generator~ inventory
            -Wallet wallet
            -Theme currentTheme
            +main(args)
            +launchGame(profileName)
            -createGameWindow()
            -updateUI()
            -rebuildShopButtons()
        }
        
        class ProfileMenu {
            +ProfileMenu(parent)
            -refreshList()
        }

        class Theme {
            <<Record>>
            +String name
            +Color accent
            +Color bgDark
        }
    }

    %% ---------------------------------------------------------
    %% PACKAGE: CORE (Business Logic)
    %% ---------------------------------------------------------
    namespace core {
        class Wallet {
            -double balance
            +earn(amount)
            +spend(amount)
            +getBalance() double
        }
        
        class PrestigeManager {
            -int prestigePoints
            +addPoints(amount)
            +getMultiplier() double
            +setPoints(amount)
            +reset()
        }
    }

    %% ---------------------------------------------------------
    %% PACKAGE: GENERATORS (Game Objects)
    %% ---------------------------------------------------------
    namespace generators {
        class Generator {
            <<Abstract>>
            #String name
            #double baseCost
            #int level
            +upgrade()
            +getCost() double
            +getLevel() int
            +getFinalProduction() double
            +calculateBaseProduction()* double
        }

        class ManualClicker { +calculateBaseProduction() }
        class GoldMine { +calculateBaseProduction() }
        class Factory { +calculateBaseProduction() }
        class Bank { +calculateBaseProduction() }
        class Temple { +calculateBaseProduction() }
    }

    %% ---------------------------------------------------------
    %% PACKAGE: UTILS (Helpers)
    %% ---------------------------------------------------------
    namespace utils {
        class SaveManager {
            +saveGame(profileName)
            +loadGame(profileName)
            +getProfiles() List
            +deleteProfile(name)
        }
        
        class ImageLoader {
            -Map~String, ImageIcon~ cache
            +getIcon(url, size)
            +preload()
        }
    }

    %% ---------------------------------------------------------
    %% RELATIONSHIPS
    %% ---------------------------------------------------------

    %% Composition: The Game OWNS the Wallet and Inventory
    JavaTycoon *-- Wallet : has 1
    JavaTycoon *-- Generator : has many (Inventory)

    %% Inheritance: All buildings ARE Generators
    Generator <|-- ManualClicker
    Generator <|-- GoldMine
    Generator <|-- Factory
    Generator <|-- Bank
    Generator <|-- Temple
    Generator <|-- Other(possible additions)

    %% Dependencies: The Game USES these utilities
    JavaTycoon ..> SaveManager : uses
    JavaTycoon ..> ImageLoader : uses
    JavaTycoon ..> PrestigeManager : uses
    JavaTycoon ..> ProfileMenu : creates

    %% Inner Class / Helper
    JavaTycoon +-- Theme : defines
    
    %% SaveManager reads/writes Core & Generator data
    SaveManager ..> Wallet : reads/writes
    SaveManager ..> Generator : reads/writes