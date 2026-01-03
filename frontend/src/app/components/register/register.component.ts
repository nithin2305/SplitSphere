import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  accountName = '';
  userId = '';
  code = '';
  confirmCode = '';
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.errorMessage = '';
    
    if (!this.accountName || !this.userId || !this.code || !this.confirmCode) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    if (this.code.length !== 4 || !/^\d+$/.test(this.code)) {
      this.errorMessage = 'Code must be exactly 4 digits';
      return;
    }

    if (this.code !== this.confirmCode) {
      this.errorMessage = 'Codes do not match';
      return;
    }

    this.authService.register({
      accountName: this.accountName,
      userId: this.userId,
      code: this.code
    }).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Registration failed. User ID might already exist.';
        console.error('Registration error:', error);
      }
    });
  }
}
