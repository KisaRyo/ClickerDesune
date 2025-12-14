usecaseDiagram
    actor "Player" as user
    package "Java Tycoon System" {
        
        usecase "Manage Profiles" as UC1
        usecase "Generate Income" as UC2
        usecase "Purchase Assets" as UC3
        usecase "Perform Prestige" as UC4
        usecase "Configure Settings" as UC5
        usecase "Save/Load Data" as UC6

    }

    user -- UC1
    user -- UC2
    user -- UC3
    user -- UC4
    user -- UC5
    
    %% Note: Save/Load is often implicit or triggered by the system/profile switch
    user -- UC6