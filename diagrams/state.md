stateDiagram
    direction TB

    %% Global States
    state "Hidden (Not in Shop)" as Hidden
    state "Visible (In Shop)" as Visible

    %% Initial Entry
    [*] --> Hidden : Game Start

    %% Transition to Shop
    Hidden --> Visible : Unlock Condition Met

    %% Complex State: The Shop Button Logic
    state Visible {
        direction LR
        
        state "Unaffordable (Red)" as NoMoney
        state "Affordable (Green)" as HasMoney
        state "Processing Purchase" as Buying

        %% Logic inside the button
        [*] --> NoMoney
        NoMoney --> HasMoney : Balance >= Cost
        HasMoney --> NoMoney : Balance < Cost
        
        HasMoney --> Buying : Click Buy
        Buying --> NoMoney : Deduct Cost & Increase Price
    }

    %% Prestige Reset
    Visible --> Hidden : Prestige Event