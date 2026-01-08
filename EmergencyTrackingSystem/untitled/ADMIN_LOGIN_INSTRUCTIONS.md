# How to Login as Admin

## Option 1: Use the Pre-created Admin Account (Recommended)

If you've run the updated `schema.sql`, a default admin account is already created:

**Username:** `admin`  
**Password:** `admin123`  
**Role:** Select **"Admin"** from the dropdown

### Steps:
1. Open the application
2. On the login page, enter:
   - Username: `admin`
   - Password: `admin123`
   - Role: Select **"Admin"** from the dropdown
3. Click "Sign In"

---

## Option 2: Create Admin Account via Registration

1. Click "Don't have an account? Register" on the login page
2. Fill in the registration form:
   - Full Name: (e.g., "System Administrator")
   - Email: (e.g., "admin@example.com")
   - Phone Number: (e.g., "+1234567890")
   - Username: (choose a username)
   - Password: (choose a password)
   - Role: Select **"Admin"** from the dropdown
   - Emergency Contact: (optional)
3. Click "Create Account"
4. Login with your new admin credentials

---

## Option 3: Create Admin Account via SQL (If Database Already Exists)

If your database already exists and you need to add ADMIN to the role enum and create an admin user:

### Step 1: Update the role ENUM
```sql
ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'RESPONDER', 'ADMIN') NOT NULL;
```

### Step 2: Insert Admin User
```sql
INSERT INTO users (full_name, email, phone_number, username, password, role, emergency_contact) 
VALUES ('Admin User', 'admin@emergency.com', '+1234567893', 'admin', 'admin123', 'ADMIN', NULL);
```

Then login with:
- Username: `admin`
- Password: `admin123`
- Role: **Admin**

---

## Important Notes:

1. **Role Selection**: Make sure to select **"Admin"** from the role dropdown on the login page. The system checks both username/password AND role.

2. **Database Update**: If your database was created before adding ADMIN support, you need to run the ALTER TABLE command above to update the role ENUM.

3. **First Time Setup**: If this is a fresh database, run the updated `schema.sql` file which includes the ADMIN role and a default admin account.

---

## Admin Dashboard Features:

Once logged in as admin, you'll have access to:
- **Dashboard**: Overview statistics
- **Users**: Manage all users (add, edit, delete)
- **Emergencies**: View and manage all emergencies
- **Statistics**: System-wide analytics
- **Profile**: Admin account information

