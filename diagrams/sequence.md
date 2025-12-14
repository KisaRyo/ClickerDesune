sequenceDiagram
    autonumber
    actor Player
    participant Main as JavaTycoon (Main)
    participant Menu as ProfileMenu (Dialog)
    participant Loader as SaveManager
    participant UI as GameWindow (JFrame)
    participant Timer as Game Loop (100ms)
    participant Wallet as Wallet

    Note over Player, Main: Phase 1: Initialization

    Player->>Main: Run Program
    activate Main
    Main->>Menu: new ProfileMenu()
    activate Menu
    Menu-->>Player: Display Profiles
    Player->>Menu: Select "Kisaryo" & Click Play
    
    Menu->>Main: launchGame("Kisaryo")
    deactivate Menu
    
    Main->>Loader: loadGame("Kisaryo")
    activate Loader
    Loader-->>Main: Returns Data (Money, Inventory, Theme)
    deactivate Loader
    
    Main->>UI: Create & Show Window
    activate UI
    
    Note over Main, Wallet: Phase 2: The Game Loop (The "Heartbeat")
    
    Main->>Timer: Start (100ms interval)
    activate Timer
    
    loop Every 0.1 Seconds
        Timer->>Main: Tick
        
        %% Passive Income Logic
        Main->>Main: calculatePassiveMPS()
        Main->>Wallet: earn(passiveAmount / 10)
        
        %% Heat Decay Logic
        Main->>Main: Decrease clickHeat (x0.90)
        
        %% Update Visuals
        Main->>UI: updateUI()
        
        %% Check Unlocks
        Main->>UI: checkPrestigeUnlock()
    end
    
    Note over Player, Wallet: Phase 3: Parallel User Input
    
    Player->>UI: Click Big Coin
    UI->>Wallet: earn(clickPower)
    UI->>Main: clickHeat + 1.0
    UI-->>Player: Update Labels