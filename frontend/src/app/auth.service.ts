import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly loggedInState = signal(this.readInitialState());

  readonly isLoggedIn = this.loggedInState.asReadonly();

  login(username: string, password: string): boolean {
    const success = username === 'admin' && password === 'admin';
    this.loggedInState.set(success);
    if (success) {
      localStorage.setItem('loggedIn', 'true');
    } else {
      localStorage.removeItem('loggedIn');
    }
    return success;
  }

  logout(): void {
    this.loggedInState.set(false);
    localStorage.removeItem('loggedIn');
  }

  private readInitialState(): boolean {
    return localStorage.getItem('loggedIn') === 'true';
  }
}

// Made with Bob
