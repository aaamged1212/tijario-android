# Google Play Data Safety Questionnaire Draft

This draft provides the answers for the Google Play Console Data Safety questionnaire based on the Tijario Android app's release manifest, Facebook SDK configuration, and database structure.

## Advertising ID Declaration
- **Does your app use Advertising ID?** YES
- **Why does your app use Advertising ID?** Analytics, Advertising or marketing / campaign measurement.

---

## Data Collection and Sharing Disclosures

### 1. Personal Information
- **Name**: Collected (for user profile and customer documents), Shared (none, backend storage only), Purpose: App functionality, Account management.
- **Email Address**: Collected (for authentication and profile), Shared (none, backend storage only), Purpose: App functionality, Account management.
- **Phone Number / WhatsApp Number**: Collected (for customer linking and profile contact), Shared (none, backend storage only), Purpose: App functionality.

### 2. User Content
- **Invoices, Quotes, Product details**: Collected, Shared (none), Purpose: App functionality.
- **Business Logos/Images**: Collected (uploaded via picker), Shared (none), Purpose: App functionality.
- **AI prompts / generated outputs**: Collected, Shared (none, processed through AI service Provider endpoints for generation), Purpose: App functionality.

### 3. App Activity
- **App Interactions (e.g. invoice created, quote created, subscriber status)**: Collected, Shared (Meta/Facebook), Purpose: Analytics, Advertising or marketing.

### 4. Device or Other IDs
- **Advertising ID, Firebase installation ID, Notification token**: Collected, Shared (Meta/Facebook, Firebase), Purpose: App functionality, Analytics, Advertising or marketing / campaign measurement.

### 5. Purchases
- **Subscription status / Purchase history**: Collected, Shared (none, synced with Google Play Billing), Purpose: App functionality.

---

## Security Practices
- **Data Encrypted in Transit**: Yes.
- **Data Sale**: No, data is not sold to third parties.
- **User Request Deletion**: Yes, through account settings or contacting support@tijario.site.

> [!WARNING]
> This is a draft declaration. The final answers in the Play Console must match the exact AAB behavior at release time.
