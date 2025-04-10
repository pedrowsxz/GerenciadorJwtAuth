import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from '../../../models/user.model';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnInit {
  userForm!: FormGroup;
  isSubmitting = false;
  isEditMode = false;
  availableRoles: string[] = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR'];
  
  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private dialogRef: MatDialogRef<UserFormComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: User
  ) { }

  ngOnInit(): void {
    this.isEditMode = !!this.data.id;
    
    this.userForm = this.fb.group({
      username: [this.data.username || '', [Validators.required, Validators.minLength(3)]],
      email: [this.data.email || '', [Validators.required, Validators.email]],
      password: ['', this.isEditMode ? [] : [Validators.required, Validators.minLength(6)]],
      roles: [this.data.roles || ['ROLE_USER']]
    });
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      return;
    }
    
    this.isSubmitting = true;
    const user: User = {
      ...this.data,
      ...this.userForm.value
    };
    
    // If editing and password is empty, remove it from the payload
    if (this.isEditMode && !user.password) {
      delete user.password;
    }
    
    if (this.isEditMode) {
      this.userService.update(this.data.id!, user).subscribe({
        next: () => {
          this.snackBar.open('User updated successfully', 'Close', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.snackBar.open('Failed to update user. ' + err.error?.message || err.message, 'Close', { duration: 5000 });
        }
      });
    } else {
      this.userService.create(user).subscribe({
        next: () => {
          this.snackBar.open('User created successfully', 'Close', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.snackBar.open('Failed to create user. ' + err.error?.message || err.message, 'Close', { duration: 5000 });
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
