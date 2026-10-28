# **:articulated_lorry: Welcome to the VIA Wiki**

This repository contains the source code, documentation, and architectural designs for our "VIA" mini transport typhoon simulation game, developed for the 2025/2026 Spring Software technology practice course.

## **:busts_in_silhouette: Team Members**

* **Dragas Vuk**
* **Grigoryan Hayk**
* **Murzin Valerii**
* **Likhtarov Andrii**

---

## **:book: Game Overview**

VIA is a simplified transportation-economic simulation game. The primary goal is for the player to organize road freight and passenger transport between predefined cities and industrial facilities to maximize profit.

The game operates in real-time on a 2-dimensional top-down grid map. Players must strategically manage their initial capital to build road networks, place stops, purchase vehicles, and establish efficient delivery routes. Success depends on balancing the continuous costs of infrastructure and vehicle maintenance against the income generated from successfully delivering goods and passengers. If the player's capital falls below zero, the company goes bankrupt and the game ends.

---

## **:dart: Chosen Sub-Tasks**

For a team of 4, the base game complexity is 1.5 units. To meet the required total project complexity of 5 units, our team has selected the following sub-tasks (totaling 4.5 extra complexity units, bringing our project total to 6.0 units):

* :evergreen_tree: **Forests \[0.5 complexity\]:** Trees appear on empty tiles and can grow or spread to adjacent empty tiles over time. Building roads through forested tiles incurs a higher clearing cost.
* :mountain: **Terrain \[1 complexity\]:** The map features varying heights represented by different slope-type tiles. Roads can only connect tiles with the same height or a difference of +-1. Vehicles move slower on uphill slopes, and the player can terraform empty spaces for a cost.
* :wrench: **Garage \[0.5 complexity\]:** Players must build garages connected to the road network. Vehicles are purchased here, automatically return for maintenance at specified intervals (which get more frequent as the vehicle ages), and can be sold when over-aged.
* :race_car: **Overtaking Vehicles \[0.5 complexity\]:** Faster vehicles can temporarily shift out of their lane to safely overtake slower vehicles, provided the oncoming lane is empty and there is no oncoming traffic within visible distance.
* :map: **Minimap \[0.5 complexity\]:** A navigable minimap interface is provided for easier orientation and scrolling across the large game world.
* :floppy_disk: **Persistence \[0.5 complexity\]:** The system supports saving the current game state to multiple save files and loading them later. After loading, vehicles that were in motion resume their journey exactly from where they were located.
* :runner: **Continuous Movement \[0.5 complexity\]:** Vehicle movement between grid tiles is rendered via smooth and continuous animation, rather than jumping jumpy block-by-block movements.

---

## **:gear: Functional Requirements**

Based on our Use Case analysis and core game rules, the software must support the following functions:

### **1. Game & View Management**

* **World Initialization:** The system must initialize a grid-based map containing pre-placed cities covering at least a 3 X 3 area and industrial facilities covering at least a 2 X 2 area.
* **Time Control:** The player must be able to pause, run at normal speed, or accelerate the game time to fast (e.g., 2x) or very fast (e.g., 4x speed).
* **State Persistence:** The player must be able to save their progress to a file and load previous states.
* **Navigation:** The player can scroll the main map in both X and Y dimensions and use a minimap to navigate.

### **2. Infrastructure Construction**

* **Road Building:** The player can build roads on empty tiles or clear forests to build, deducting capital. Roads cannot overlap facilities or cities, but can connect to their edges.
* **Terraforming:** The player can pay to change the elevation of empty spaces by 1 level.
* **Stops & Garages:** The player can place 1-tile stops along the road network near cities/facilities and build garages for vehicle spawning/maintenance.

### **3. Company & Route Management**

* **Vehicle Logistics:** The player can purchase distinct transport vehicles for at least 3 different types of raw materials/products, plus buses for passengers. There must be at least 2 different vehicle types per category with varying stats.
* **Routing:** The player can assign a continuous, circular route (e.g., A =\> B =\> C =\> A) to a vehicle.

### **4. Background Simulation & Economy**

* **Production & Demand:** Industrial facilities must automatically consume certain goods and produce others. Cities demand passengers and/or finished products, and these demands slowly increase or decrease over time.
* **Movement System:** Vehicles must navigate the road network, load cargo/passengers, and adhere to a maximum of 2 vehicles per tile (one per direction) unless performing a safe overtaking maneuver.
* **Financial Engine:** The system must automatically deduct maintenance costs, process income upon successful deliveries, and trigger a bankruptcy state if the balance falls below zero. Vehicles must automatically seek out garages for maintenance based on age.

---

## **:computer: Non-Functional Requirements**

* **Programming Language:** Java (Standard choice for Object-Oriented simulation projects).
* **Game Engine:** libGDX(a free and open-source game-development application framework written in the Java programming)
* **Architecture:** Model-View-Controller (MVC) pattern to strictly separate the backend simulation engine from the frontend rendering.
* **Version Control & Collaboration:** Git via GitLab, utilizing branching for feature development.
* **Performance:** The pathfinding algorithm and simulation tick must be optimized to run without lagging, even when accelerated to 4x speed.

---

1. [USE CASE](https://szofttech.inf.elte.hu/software-technology-2026/group-01/via/-/wikis/home/USE-CASE)
2. [WIREFRAME](https://szofttech.inf.elte.hu/software-technology-2026/group-01/via/-/wikis/06_UI_Wireframes)
3. [CLASS DIAGRAM](https://szofttech.inf.elte.hu/software-technology-2026/group-01/via/-/wikis/home/%7BCLASS_DIAGRAM%7D)
4. [USER STORIES](https://szofttech.inf.elte.hu/software-technology-2026/group-01/via/-/wikis/home/USER-STORIES)
5. [GAMEPLAY VIDEO ](https://jumpshare.com/share/EHDVw1FrJCXDrULmpcgv?b=P43GB6v8rf8BhRJQNrKV)
