# Mentor-Mentee-Connect
Mentor Connect is a software engineering project designed to bridge the gap between mentors and mentees by providing a structured platform for collaboration, guidance, and knowledge sharing. The application enables mentees to discover suitable mentors based on domain expertise and facilitates seamless communication and mentorship management.

## Overview
Mentor Connect is a software engineering project designed to bridge the gap between **mentors and mentees**. The platform allows mentees to search for mentors based on domain expertise, send mentorship requests, book sessions, and interact through chat.

## Features
* 🔐 User Authentication – Secure login and registration for mentors and mentees
* 🔍 Mentor Search – Find mentors based on domain and expertise
* 📩 Mentorship Requests – Send, accept, or reject requests
* 📅 Slot Booking – Schedule sessions between mentor and mentee
* 💬 Chat System – Real-time communication between users
* ⭐ Feedback & Rating – Mentees can rate mentors after sessions
* 🔔 Notifications – Get updates on requests, messages, and bookings
* 👤 Profile Management – Editable mentor and mentee profiles

## Architecture
The system follows a 3-Tier Architecture:
* Presentation Layer → Java UI (Swing / JavaFX / JSP)
* Application Layer → Business Logic (Controllers & Services)
* Data Layer → MySQL Database

## Tech Stack
* Frontend : Java (Swing / JavaFX / JSP-Servlets)
* Backend : Java (Core + JDBC)
* Database : MySQL
* IDE: IntelliJ IDEA


## Project Structure

```
mentor-connect/
│
├── src/                # Source code
├── database/           # SQL schema and queries
├── screenshots/        # Application screenshots
├── docs/               # UML diagrams & documentation
├── README.md
└── .gitignore
```


## How to Run
1. Clone the repository:
```
git clone https://github.com/your-username/mentor-connect.git
```

2. Open the project in **IntelliJ IDEA**

3. Configure **MySQL Database**
   * Create database
   * Import schema from `/database/schema.sql`

4. Update database credentials in:
```
DatabaseConnection.java
```

5. Run the project:
   * Execute the **Main class**


##  Screenshots
screenshots inside `/screenshots` folder

Example:
* Login Page
* Dashboard
* Chat Window
* Mentor Search

## Documentation
Available in `/docs` folder:
* SDD (Software Design Document)
* UML Diagrams (Use Case, Class, ER, Component, Deployment)
* Architecture Diagram


## Objective
To build a scalable and user-friendly platform that improves **mentor-mentee interaction**, reduces manual effort, and enhances learning collaboration.


## Future Enhancements
* 🤖 AI-based mentor recommendation
* 📹 Video conferencing integration
* 📱 Mobile application support
* 📊 Analytics dashboard

## Developed By
Shinnmaya KB
Abirami K
Rakchitha M

## License
This project is open-source and available under the **MIT License**.

