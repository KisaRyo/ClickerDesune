graph TD
    %% NODES
    Start([Start Application])
    End([Exit Game])
    
    %% PROFILE PHASE
    ProfileInit[Initialize Profile Menu]
    DecProfile{Profile Exists?}
    CreateProf[Create New Profile]
    LoadProf[Load .properties File]
    
    %% MAIN LOOP ENTRY
    InitGame[Initialize UI & Timers]
    Idle((Idle / Wait))
    
    %% EVENT BRANCHES
    
    %% 1. TIMER EVENT
    EventTimer[Event: 100ms Tick]
    CalcPassive[Calculate Passive MPS]
    UpdateWallet1[Add Passive Income to Wallet]
    DecayHeat[Decay Click Heat]
    CheckPrestige[Check if Prestige Unlocks]
    
    %% 2. CLICK EVENT
    EventClick[Event: Click Big Coin]
    CalcActive[Calculate Click Power]
    UpdateWallet2[Add Active Income to Wallet]
    IncHeat[Increase Click Heat]
    
    %% 3. BUY EVENT
    EventBuy[Event: Buy Upgrade]
    CheckCost{Balance >= Cost?}
    DeductFunds[Deduct Cost from Wallet]
    UpgradeItem[Increase Level & Production]
    AddItem[Add to Inventory List]
    ShowError[Log: 'Too Expensive']
    
    %% 4. PRESTIGE EVENT
    EventPrestige[Event: Click Prestige]
    CheckThresh{Balance >= $5000?}
    CalcPoints[Convert Balance to Points]
    ResetState[Reset Wallet, Inventory, Theme]
    AddMult[Apply Permanent Multiplier]
    
    %% UI UPDATE (Convergence)
    UpdateUI[Update UI Labels & Shop Buttons]
    
    %% AUTO SAVE
    EventSave[Event: 30s Timer or Exit]
    WriteFile[Write to .properties]

    %% CONNECTIONS
    Start --> ProfileInit
    ProfileInit --> DecProfile
    DecProfile -- No --> CreateProf
    DecProfile -- Yes --> LoadProf
    CreateProf --> LoadProf
    LoadProf --> InitGame
    InitGame --> Idle
    
    %% Triggering Events
    Idle -.-> EventTimer
    Idle -.-> EventClick
    Idle -.-> EventBuy
    Idle -.-> EventPrestige
    Idle -.-> EventSave
    
    %% Timer Flow
    EventTimer --> CalcPassive
    CalcPassive --> UpdateWallet1
    UpdateWallet1 --> DecayHeat
    DecayHeat --> CheckPrestige
    CheckPrestige --> UpdateUI
    
    %% Click Flow
    EventClick --> CalcActive
    CalcActive --> UpdateWallet2
    UpdateWallet2 --> IncHeat
    IncHeat --> UpdateUI
    
    %% Buy Flow
    EventBuy --> CheckCost
    CheckCost -- Yes --> DeductFunds
    DeductFunds --> UpgradeItem
    UpgradeItem --> AddItem
    AddItem --> UpdateUI
    CheckCost -- No --> ShowError
    ShowError --> Idle
    
    %% Prestige Flow
    EventPrestige --> CheckThresh
    CheckThresh -- Yes --> CalcPoints
    CalcPoints --> ResetState
    ResetState --> AddMult
    AddMult --> UpdateUI
    CheckThresh -- No --> ShowError
    
    %% Save Flow
    EventSave --> WriteFile
    WriteFile --> Idle
    
    %% Closing the loop
    UpdateUI --> Idle