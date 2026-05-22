# Integration Contract Addendum (Hasil Panen)

## Hasil Panen Ownership
Hasil Panen owns:
- harvest report data
- approval/rejection status
- transport eligibility view
- payroll outbox payload generation on approval

## Required Validation Rules
Hasil Panen must enforce:
- Buruh can submit only for themselves (identity from authenticated context, not request body)
- Buruh can submit only once per day
- Submission stores assignment snapshot:
  - `buruhId` (Auth Long)
  - `mandorId` (Auth Long)
  - `kebunCode`
  - `kebunId` if available
- Mandor can approve/reject only Buruh under their supervision (Auth assignment check)
- Mandor approve/reject must validate Mandor kebun context (Kebun assignment check)
- Rejection requires reason
- Only `APPROVED` harvest is transport-eligible
- Approval must create payroll/outbox payload with sufficient business data

## Identifier Alignment
- Cross-service user IDs must use Auth `Long` IDs.
- Kebun reference must use `kebunCode` as canonical key in current phase.

## Upstream Dependencies
- Auth provides identity and Buruh-Mandor assignment read APIs.
- Kebun provides Mandor-Kebun read API.
