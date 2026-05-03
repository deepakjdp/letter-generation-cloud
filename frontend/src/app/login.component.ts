import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-body">
      <div class="login-container">
        <h2>Letter Generation System</h2>
        <form (ngSubmit)="login()">
          <div class="form-row">
            <label for="username">Username</label>
            <input id="username" name="username" [(ngModel)]="username" required />
          </div>
          <div class="form-row">
            <label for="password">Password</label>
            <input id="password" name="password" type="password" [(ngModel)]="password" required />
          </div>
          <div class="form-row">
            <button type="submit" class="btn-primary">Login</button>
          </div>
          <p *ngIf="loginFailed" class="message-error">Invalid username or password.</p>
        </form>
      </div>
    </div>
  `
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  username = '';
  password = '';
  loginFailed = false;

  login(): void {
    const success = this.authService.login(this.username, this.password);
    this.loginFailed = !success;
    if (success) {
      void this.router.navigate(['/secured/home']);
    }
  }
}

// Made with Bob
