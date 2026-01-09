import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { GroupService } from '../../services/group.service';
import { ExpenseService } from '../../services/expense.service';
import { BalanceService } from '../../services/balance.service';
import { SettlementService } from '../../services/settlement.service';
import { TransactionService } from '../../services/transaction.service';
import { ReportService } from '../../services/report.service';
import { Group, Expense, Balance, Transaction } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  currentUser: string = '';
  currentUserId: string = '';
  groups: Group[] = [];
  selectedGroup: Group | null = null;
  expenses: Expense[] = [];
  balances: Balance[] = [];
  transactions: Transaction[] = [];
  
  // Forms
  showCreateGroup = false;
  showJoinGroup = false;
  showCreateExpense = false;
  showSettlement = false;
  showTransactionHistory = false;
  
  newGroupName = '';
  joinCode = '';
  
  newExpenseDescription = '';
  newExpenseAmount = 0;
  selectedParticipants: { [userId: string]: boolean } = {};
  
  // Settlement form
  settlementPayeeUserId = '';
  settlementAmount = 0;
  settlementNote = '';
  
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private groupService: GroupService,
    private expenseService: ExpenseService,
    private balanceService: BalanceService,
    private settlementService: SettlementService,
    private transactionService: TransactionService,
    private reportService: ReportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.currentUser = user.accountName;
        this.currentUserId = user.userId;
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
    this.loadGroupTransactions();
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

  loadGroupTransactions(): void {
    if (this.selectedGroup) {
      this.transactionService.getGroupTransactions(this.selectedGroup.id).subscribe({
        next: (transactions) => {
          this.transactions = transactions;
        },
        error: (error) => {
          console.error('Error loading transactions:', error);
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

    if (this.selectedGroup.closed) {
      this.errorMessage = 'Cannot add expense to a closed group';
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
        this.loadGroupTransactions(); // Refresh transactions
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = 'Failed to create expense';
        console.error('Error creating expense:', error);
      }
    });
  }

  createSettlement(): void {
    this.errorMessage = '';
    this.successMessage = '';
    
    if (!this.selectedGroup) {
      this.errorMessage = 'Please select a group first';
      return;
    }

    if (this.selectedGroup.closed) {
      this.errorMessage = 'Cannot add settlement to a closed group';
      return;
    }

    if (!this.settlementPayeeUserId || this.settlementAmount <= 0) {
      this.errorMessage = 'Please enter valid settlement details';
      return;
    }

    if (this.settlementPayeeUserId === this.currentUserId) {
      this.errorMessage = 'Cannot settle with yourself';
      return;
    }

    this.settlementService.createSettlement({
      groupId: this.selectedGroup.id,
      payeeUserId: this.settlementPayeeUserId,
      amount: this.settlementAmount,
      note: this.settlementNote
    }).subscribe({
      next: () => {
        this.settlementPayeeUserId = '';
        this.settlementAmount = 0;
        this.settlementNote = '';
        this.showSettlement = false;
        this.successMessage = 'Settlement recorded successfully!';
        this.loadGroupBalances(); // Refresh balances
        this.loadGroupTransactions(); // Refresh transactions
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to create settlement';
        console.error('Error creating settlement:', error);
      }
    });
  }

  closeGroup(): void {
    if (!this.selectedGroup) {
      return;
    }

    if (this.selectedGroup.closed) {
      this.errorMessage = 'Group is already closed';
      return;
    }

    const confirmed = confirm(`Are you sure you want to close "${this.selectedGroup.name}"? This action cannot be undone.`);
    if (!confirmed) {
      return;
    }

    this.groupService.closeGroup(this.selectedGroup.id).subscribe({
      next: (group) => {
        this.selectedGroup = group;
        // Update the group in the list
        const index = this.groups.findIndex(g => g.id === group.id);
        if (index !== -1) {
          this.groups[index] = group;
        }
        this.successMessage = 'Group closed successfully!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to close group';
        console.error('Error closing group:', error);
      }
    });
  }

  downloadExcelReport(): void {
    if (!this.selectedGroup) {
      return;
    }

    this.reportService.downloadExcelReport(this.selectedGroup.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.selectedGroup!.name}_report.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        this.successMessage = 'Excel report downloaded!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = 'Failed to download Excel report';
        console.error('Error downloading Excel report:', error);
      }
    });
  }

  downloadPdfReport(): void {
    if (!this.selectedGroup) {
      return;
    }

    this.reportService.downloadPdfReport(this.selectedGroup.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.selectedGroup!.name}_report.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        this.successMessage = 'PDF report downloaded!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = 'Failed to download PDF report';
        console.error('Error downloading PDF report:', error);
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
    this.showSettlement = false;
    this.showTransactionHistory = false;
    this.newGroupName = '';
    this.joinCode = '';
    this.newExpenseDescription = '';
    this.newExpenseAmount = 0;
    this.selectedParticipants = {};
    this.settlementPayeeUserId = '';
    this.settlementAmount = 0;
    this.settlementNote = '';
    this.errorMessage = '';
    this.successMessage = '';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
