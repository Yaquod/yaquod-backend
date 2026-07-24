# Yaquod Backend

The backend service for the Yaquod ride-hailing/autonomous robo-taxi platform.

![System Design](images/system_design.svg)

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Features](#features)
  - [Authentication and Account Management](#authentication-and-account-management)
  - [Trip Management](#trip-management)
  - [Real-Time Communication](#real-time-communication)
  - [Payment Integration](#payment-integration)
  - [Admin Dashboard](#admin-dashboard)
  - [Push Notifications](#push-notifications)
- [Getting Started](#getting-started)
- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Security](#security)
- [Deployment](#deployment)
- [License](#license)

## Overview

This is the backend service for the Yaquod ride-hailing/autonomous robo-taxi platform built with Spring Boot. It connects passengers directly with vehicles through a complete lifecycle of trip requests, vehicle matching, real-time tracking, payment processing, and post-trip rating.

The system supports multiple authentication methods including email and password, Google OAuth, and API-key-based vehicle authentication.

The system is designed for production deployment with PostgreSQL and PostGIS for spatial data handling, Redis for caching and timeout management, MQTT for real-time vehicle communication, and Firebase Cloud Messaging for push notifications to mobile clients. Payment processing is handled through Paymob with support for card tokenization, saved cards, and recurring charges.
