import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { GroupService } from '../../services/group.service';
import { ExpenseService } from '../../services/expense.service';
import { BalanceService } from '../../services/balance.service';
import { Group, Expense, Balance } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  currentUser: string = '';
  groups: Group[] = [];
  selectedGroup: Group | null = null;
  expenses: Expense[] = [];
  balances: Balance[] = [];
  
  // Forms
  showCreateGroup = false;
  showJoinGroup = false;
  showCreateExpense = false;
  
  newGroupName = '';
  joinCode = '';
  
  newExpenseDescription = '';
  newExpenseAmount = 0;
  selectedParticipants: { [userId: string]: boolean } = {};
  
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private groupService: GroupService,
    private expenseService: ExpenseService,
    private balanceService: BalanceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.currentUser = user.accountName;
      } else {
        this.router.navigate(['/login']);
      }
    });
    
    this.loadGroups();
  }

  loadGroups(): void {
    this.groupService.getUserGroups().subscribe({
      next: (groups) => {
        this.groups = groups;
      },
      error: (error) => {
        console.error('Error loading groups:', error);
      }
    });
  }

  selectGroup(group: Group): void {
    this.selectedGroup = group;
    this.loadGroupExpenses();
    this.loadGroupBalances();
    this.resetForms();
  }

  loadGroupExpenses(): void {
    if (this.selectedGroup) {
      this.expenseService.getGroupExpenses(this.selectedGroup.id).subscribe({
        next: (expenses) => {
          this.expenses = expenses;
        },
        error: (error) => {
          console.error('Error loading expenses:', error);
        }
      });
    }
  }

  loadGroupBalances(): void {
    if (this.selectedGroup) {
      this.balanceService.getGroupBalances(this.selectedGroup.id).subscribe({
        next: (balances) => {
          this.balances = balances;
        },
        error: (error) => {
          console.error('Error loading balances:', error);
        }
      });
    }
  }

  createGroup(): void {
    this.errorMessage = '';
    this.successMessage = '';
    
    if (!this.newGroupName.trim()) {
      this.errorMessage = 'Please enter a group name';
      return;
    }

    this.groupService.createGroup({ name: this.newGroupName }).subscribe({
      next: (group) => {
        this.groups.push(group);
        this.newGroupName = '';
        this.showCreateGroup = false;
        this.successMessage = 'Group created successfully!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = 'Failed to create group';
        console.error('Error creating group:', error);
      }
    });
  }

  joinGroup(): void {
    this.errorMessage = '';
    this.successMessage = '';
    
    if (!this.joinCode.trim()) {
      this.errorMessage = 'Please enter a join code';
      return;
    }

    this.groupService.joinGroup(this.joinCode).subscribe({
      next: (group) => {
        this.groups.push(group);
        this.joinCode = '';
        this.showJoinGroup = false;
        this.successMessage = 'Joined group successfully!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = 'Invalid join code or already a member';
        console.error('Error joining group:', error);
      }
    });
  }

  createExpense(): void {
    this.errorMessage = '';
    this.successMessage = '';
    
    if (!this.selectedGroup) {
      this.errorMessage = 'Please select a group first';
      return;
    }

    if (!this.newExpenseDescription.trim() || this.newExpenseAmount <= 0) {
      this.errorMessage = 'Please enter valid expense details';
      return;
    }

    const participantUserIds = Object.keys(this.selectedParticipants)
      .filter(userId => this.selectedParticipants[userId]);

    if (participantUserIds.length === 0) {
      this.errorMessage = 'Please select at least one participant';
      return;
    }

    this.expenseService.createExpense({
      description: this.newExpenseDescription,
      amount: this.newExpenseAmount,
      groupId: this.selectedGroup.id,
      participantUserIds: participantUserIds
    }).subscribe({
      next: (expense) => {
        this.expenses.unshift(expense);
        this.newExpenseDescription = '';
        this.newExpenseAmount = 0;
        this.selectedParticipants = {};
        this.showCreateExpense = false;
        this.successMessage = 'Expense created successfully!';
        this.loadGroupBalances(); // Refresh balances
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = 'Failed to create expense';
        console.error('Error creating expense:', error);
      }
    });
  }

  toggleParticipant(userId: string): void {
    this.selectedParticipants[userId] = !this.selectedParticipants[userId];
  }

  resetForms(): void {
    this.showCreateGroup = false;
    this.showJoinGroup = false;
    this.showCreateExpense = false;
    this.newGroupName = '';
    this.joinCode = '';
    this.newExpenseDescription = '';
    this.newExpenseAmount = 0;
    this.selectedParticipants = {};
    this.errorMessage = '';
    this.successMessage = '';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
