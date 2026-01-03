import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  userId = '';
  code = '';
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.errorMessage = '';
    
    if (!this.userId || !this.code) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    if (this.code.length !== 4 || !/^\d+$/.test(this.code)) {
      this.errorMessage = 'Code must be exactly 4 digits';
      return;
    }

    this.authService.login({ userId: this.userId, code: this.code }).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage = 'Invalid credentials';
        console.error('Login error:', error);
      }
    });
  }
}
