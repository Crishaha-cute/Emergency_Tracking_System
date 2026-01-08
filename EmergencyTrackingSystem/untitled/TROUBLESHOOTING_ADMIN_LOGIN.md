# Troubleshooting Admin Login Issues

If you're getting "Invalid credentials or role!" error, follow these steps:

## Step 1: Check Database Role ENUM

The database table needs to have 'ADMIN' in the role ENUM. Run this SQL:

```sql
ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'RESPONDER', 'ADMIN') NOT NULL;
```

**How to run SQL:**
1. Open phpMyAdmin (usually at http://localhost/phpmyadmin)
2. Select `emergency_tracking_system` database
3. Click on SQL tab
4. Paste the ALTER TABLE command above
5. Click "Go"

## Step 2: Create Admin User

Run this SQL to create an admin user:

```sql
INSERT INTO users (full_name, email, phone_number, username, password, role, emergency_contact) 
VALUES ('Admin User', 'admin@emergency.com', '+1234567893', 'admin', 'admin123', 'ADMIN', NULL);
```

Or use the ready-to-run script: **FIX_ADMIN_LOGIN.sql**

## Step 3: Verify Admin User Exists

Check if admin user was created:

```sql
SELECT * FROM users WHERE role = 'ADMIN';
```

You should see at least one user with role = 'ADMIN'.

## Step 4: Login Steps

1. **Username:** `admin`
2. **Password:** `admin123`
3. **Role dropdown:** Select **"Admin"** (this is case-sensitive in the dropdown!)
4. Click "Sign In"

## Common Issues:

### Issue 1: "Admin" option not in dropdown
- Make sure you've restarted the application after code changes
- The dropdown should show: User, Responder, Admin

### Issue 2: Database connection error
- Make sure MySQL is running
- Check DatabaseConnection.java configuration
- Verify database name is `emergency_tracking_system`

### Issue 3: Role ENUM not updated
- The ALTER TABLE command must be run successfully
- Check with: `SHOW COLUMNS FROM users WHERE Field = 'role';`
- Should show: `enum('USER','RESPONDER','ADMIN')`

### Issue 4: Wrong credentials
- Default admin credentials:
  - Username: `admin`
  - Password: `admin123`
  - Role: `Admin` (in dropdown)

### Issue 5: User exists but wrong role
If a user exists but has wrong role:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```

## Quick Test Query

Run this to test if everything is set up correctly:

```sql
SELECT username, password, role 
FROM users 
WHERE username = 'admin' 
  AND password = 'admin123' 
  AND role = 'ADMIN';
```

If this returns a row, login should work!

## Alternative: Register New Admin

If SQL is difficult, you can register a new admin:

1. Click "Don't have an account? Register"
2. Fill in all fields
3. **Important:** Select "Admin" from the Role dropdown
4. Click "Create Account"
5. Login with those credentials

**Note:** Registration only works if the database ENUM has been updated first!

