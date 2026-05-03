import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AuthService } from './auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  template: `
    <div class="app-shell">
      <div class="sidebar">
        <h3>Menu</h3>
        <a routerLink="/secured/view-letters">View Letters</a>
        <a routerLink="/secured/generate-letter">Generate Letters</a>
        <a routerLink="/secured/search-letters">Search Letters</a>
        <button type="button" class="sidebar-link" (click)="logout()">Logout</button>
      </div>
      <div class="content">
        <router-outlet></router-outlet>
      </div>
    </div>
  `
})
export class AppShellComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}

// Made with Bob
