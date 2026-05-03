import { Routes } from '@angular/router';

import { AppShellComponent } from './app-shell.component';
import { authGuard } from './auth.guard';
import { GenerateLetterComponent } from './generate-letter.component';
import { HomeComponent } from './home.component';
import { LoginComponent } from './login.component';
import { SearchLettersComponent } from './search-letters.component';
import { ViewLettersComponent } from './view-letters.component';

export const appRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'secured',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'home'
      },
      {
        path: 'home',
        component: HomeComponent
      },
      {
        path: 'view-letters',
        component: ViewLettersComponent
      },
      {
        path: 'generate-letter',
        component: GenerateLetterComponent
      },
      {
        path: 'search-letters',
        component: SearchLettersComponent
      }
    ]
  }
];

// Made with Bob
