# PromptForge: Distributed AI Code Workspace Platform
> **Tagline: AI-Powered Code Generator**

PromptForge is a collaborative full-stack AI workspace (inspired by tools like v0.dev and Bolt.new) that lets users generate, preview, and edit full-stack code bases in real-time through simple conversational prompts. 

Unlike single-service AI applications, PromptForge is architected as a distributed microservice backend. It coordinates real-time AI code generation, distributed file storage, project-based access controls, and user subscription limits using safe transactional boundaries and asynchronous messaging patterns.

---

## 💡 What PromptForge Does (Core Features)

Here is a look at what the platform does from a user's perspective:

### 1. AI Coding Agent & Streaming Workspace
- **Dynamic File Tree Generation**: When you type a prompt (e.g., *"build a React calculator app"*), the system doesn't just return markdown text. It generates complete files and builds a visual directory tree of your project workspace.
- **Server-Sent Events (SSE)**: The AI's code output streams directly to your browser block-by-block, providing immediate visual feedback during generation.
- **Smart Code Parsing**: The backend automatically parses the LLM output to extract code blocks and determine which files need to be created or modified.

### 2. Live File System & Workspace Storage
- **Workspace Tree Management**: A dedicated service tracks project structures and sends structured directory layouts (file tree) to the frontend.
- **Direct S3 Object Storage**: All created files are physically written to and read from S3-compatible storage (MinIO), allowing the workspace to support large projects without cluttering local databases.
- **Physical File Lifecycle Cleanup**: Deleting a project permanently purges the associated folder prefix from the S3 bucket to prevent database-storage mismatches.
- **Template Safety Rollback**: When template copy operations fail during project initialization, the system automatically rolls back by purging the partially created folder on MinIO to prevent storage leaks.

### 3. User Identity & Account Security
- **Secure Onboarding**: User accounts are secured using JWT-based authentication. 
- **Verification OTPs**: Caches short-lived OTP tokens in Redis to handle email signup verifications and password reset flows safely.
- **Transactional User Deletion Saga**: Supports secure user deletion by first blocking and soft-deleting the user, triggering asynchronous S3 and workspace database cleanup across microservices via Kafka, and finally hard-deleting the user profile and subscription metadata.

### 4. Subscription Plans & Razorpay Checkout
- **Tiered Access Tiers**: Offers Free, Pro, and Enterprise subscription tiers.
- **Token Quota Enforcement**: Daily AI token usage is audited before each prompt call. Users are blocked or prompted to upgrade if they exceed their plan limits.
- **Razorpay Checkout Flow**: Initiates secure checkout orders and validates payments securely by verifying incoming Razorpay webhook signatures before upgrading subscription status.

### 5. Multi-User Collaboration & Permissions
- **Collaborator Invites**: Project owners can invite other users to join their workspace by email.
- **Role-Based Security**: Restricts actions (viewing, editing, deleting, or managing members) based on project roles (Owner, Editor, Viewer).

---

## 🏗️ System Design Architecture

Here is how the services connect, exchange events, and handle client requests.

![PromptForge Architecture](PromptForge%20architecture.png)

> 📂 **Need more visual breakdowns?** Refer to this [Google Drive folder](https://drive.google.com/drive/folders/1nXlqrJ6HnDvM2Gt9oyDqtZx6QcNov21L?usp=drive_link) containing additional system design diagrams.

### 📦 Microservices Breakdown & Service Map

#### 1. Discovery Service (`discovery-service` | Port `8761`)
- **Role**: Service Registration & Lookup Server (Eureka).
- **Responsibilities**:
  - Dynamically registers all microservice instances as they boot up.
  - Enables client-side load balancing via Spring Cloud LoadBalancer, allowing microservices to call each other securely by service name (e.g. `account-service` or `workspace-service`) instead of using hardcoded IP addresses.

#### 2. Config Service (`config-service` | Port `8888`)
- **Role**: Centralized Configuration Repository.
- **Responsibilities**:
  - Serves centralized external configurations to all backend services at startup.
  - Pulls properties dynamically from the [config-repo](file:///c:/Users/mayur/Desktop/PromptForge/config-repo) folder, making it easy to adjust environment properties, Kafka topics, database connection pools, and feature flags without rebuilding code.

#### 3. API Gateway (`api-gateway` | Port `8080`)
- **Role**: Secure Routing & Entry Proxy (Spring Cloud Gateway).
- **Responsibilities**:
  - Serves as the single secure entry point for all frontend and external calls.
  - Inspects incoming JWT authentication tokens programmatically to enforce centralized authentication blocks.
  - Handles global CORS policy overrides.
  - Routes requests dynamically to appropriate destination microservices based on URL subpaths (e.g., `/api/auth/**` -> `account-service`, `/api/projects/**` -> `workspace-service`, `/api/chat/**` -> `intelligence-service`).

#### 4. Account Service (`account-service` | Port `9050`)
- **Role**: User Security, Subscriptions, & Admin Control.
- **Responsibilities**:
  - Coordinates secure logins, signups, password resets, and validation OTP caching in Redis.
  - Tracks user roles, plan tiers (Free, Pro, Growth), and enforces daily limit boundaries.
  - Communicates directly with the **Razorpay Payment API** to generate client orders and securely verify webhook payment confirmation signatures.
  - Serves the **Admin Dashboard** APIs to let system admins search user listings, block/unblock accounts, and configure subscription plans.
  - Acts as the **User Deletion Saga Orchestrator**, initiating deletion requests on `user-deletion-request-event` and consuming response events on `user-deletion-response-event` to finalize deletion and cleanup.

#### 5. Workspace Service (`workspace-service` | Port `9020`)
- **Role**: Project Workspace & Storage Coordinator.
- **Responsibilities**:
  - Manages workspace directory hierarchies, templates, and collaborator access control.
  - Communicates with **MinIO S3 storage** buckets to read/write generated project files and pack workspace directories into downloadable zip archives.
  - Restricts access rules (`canViewProject`, `canEditProject`) before allowing code views or write operations.
  - Functions as a **Saga Participant**, consuming code edits from Kafka (`file-storage-request-event`), writing changes to S3, and broadcasting status receipts.
  - Consumes deletion requests on `user-deletion-request-event` to purge database metadata and MinIO project storage directories for deleted users, replying back to the orchestrator.
  - Implements compensating fallback deletes in `ProjectTemplateServiceImpl` to prevent storage leaks if template instantiation fails.

#### 6. Intelligence Service (`intelligence-service` | Port `9030`)
- **Role**: LLM Handler & Saga Orchestrator.
- **Responsibilities**:
  - Connects to NVIDIA AI / OpenAI APIs to stream conversational code suggestions block-by-block back to the browser using Server-Sent Events (SSE).
  - Parses LLM output to extract code files, identifying exactly what files to write or modify.
  - Acts as the **Saga Orchestrator**, committing the transactional chat changes locally and managing the distributed outbox loop to Kafka.
  - Runs the background **Saga Cleanup Scheduler** to sweep and fail stale transactions after 5 minutes of inactivity.

#### 7. Common Library (`common-lib` | Shared Dependency)
- **Role**: Shared Utility & Domain Library.
- **Responsibilities**:
  - Centralizes domain DTOs (e.g. `UserDto`, `PlanDto`, `FileTreeDto`, `UsageSnapshotDto`) and permission enums (`ProjectPermission`, `ProjectRole`) to ensure strict compile-time interface compatibility for OpenFeign bindings.
  - Defines the core Kafka event schemas (`FileStoreRequestEvent`, `FileStoreResponseEvent`, `UserDeletionRequestEvent`, `UserDeletionResponseEvent`, `EmailEvent`) used during distributed transactions and events.
  - Implements the centralized REST Controller Advice (`GlobalExceptionHandler`) to provide consistent error responses across all services and block stack trace leaks.
  - Exposes the shared security helper (`AuthUtil`) to extract authenticated context (user IDs) from gateway-forwarded JWT requests uniformly.

- **Client App (React)**: Running on Port `5173` (Local frontend editor workspace).

---

## 🛠️ Resiliency and System Patterns

### 1. Asynchronous Saga Pattern (Transactional Outbox)
The platform ensures distributed data consistency across multiple databases and object storage buckets using asynchronous Saga flows:

#### Flow A: AI Code Generation & File Storage (Outbox Pattern)
When a user asks the AI to edit code, the platform must guarantee that database records (chat logs) and physical file writes (in MinIO) stay consistent:
- **Write Consistency**: When file changes are generated, `intelligence-service` saves the messages and a `PENDING` chat event within a local DB transaction. 
- **Outbox Publishing**: It publishes a `FileStoreRequestEvent` to Kafka **only after** the database transaction commits successfully.
- **Idempotent Storage**: `workspace-service` consumes the message, checks its processed idempotency list to avoid double-processing, writes the files to MinIO, and publishes a `FileStoreResponseEvent` back to Kafka.
- **State Transition**: `intelligence-service` consumes this response and marks the saga `CONFIRMED` or `FAILED`.
- **Auto-Recovery Sweep**: If a network failure stalls the message flow, a background task (`SagaCleanupScheduler`) sweeps the database every minute and fails any pending events older than 5 minutes to release system resources.

#### Flow B: Distributed User Deletion
When an administrator deletes a user, the system triggers a Saga to clean up resources across services:
- **Phase 1 (Block & Request)**: `account-service` blocks/soft-deletes the user, evicts their active Redis plan cache, and publishes a `UserDeletionRequestEvent` to Kafka.
- **Phase 2 (Workspace Cleanup)**: `workspace-service` consumes the request event, searches for project memberships, deletes metadata from the database, deletes the physical S3 project directory in MinIO, and publishes a `UserDeletionResponseEvent` with status (success/failure).
- **Phase 3 (Permanent Removal)**: `account-service` consumes the response. If cleanup succeeded, it deletes the user's subscription history to satisfy foreign key constraints and permanently hard-deletes the user.

### 2. Project Template Rollback
When template copy operations fail during project instantiation from a template, `ProjectTemplateServiceImpl` executes compensating rollback logic, deleting any partially copied files from MinIO to prevent storage leaks.

### 3. Resilience4j Circuit Breakers
To prevent a single service outage from bringing down the entire platform, all OpenFeign calls are wrapped with Resilience4j circuit breakers:
- **Workspace -> Account**: If the account service fails, project limits default to a standard `FREE` plan limit (5 projects) so users can still create workspaces.
- **Intelligence -> Workspace**: If the workspace service goes offline during an AI stream, the AI chat falls back to returning a `"Workspace service unavailable"` warning instead of terminating the conversation.
- **Account -> Intelligence**: If the intelligence service fails to return today's token counter, billing checkouts fallback to 0 tokens consumed to avoid blocking checkout flows.

---

## 🚀 Running PromptForge Locally

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- Docker & Docker Compose

### Step 1: Spin Up Infrastructure
Start the database, message broker, cache, and object storage:
```bash
docker-compose up -d
```
*Note: A startup script automatically initializes MinIO and creates the `projects` and `templates` S3 buckets.*

### Step 2: Build the Modules
Use the local Maven wrappers inside each service directory to build the project:
```bash
# Build and install the common dependency library
cd common-lib && ./mvnw clean install -DskipTests

# Build the config repository, registry, gateway, and microservices
cd ../config-service && ./mvnw clean compile
cd ../discovery-service && ./mvnw clean compile
cd ../api-gateway && ./mvnw clean compile
cd ../account-service && ./mvnw clean compile
cd ../workspace-service && ./mvnw clean compile
cd ../intelligence-service && ./mvnw clean compile
```

### Step 3: Service Startup Order
Start the services in the following order:
1. **Config Service** (`config-service` on port `8888`)
2. **Discovery Service** (`discovery-service` on port `8761`)
3. **API Gateway** (`api-gateway` on port `8080`)
4. **Backend Services** (`account-service`, `workspace-service`, `intelligence-service`)
