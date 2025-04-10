import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Product } from '../../../models/product.model';
import { ProductService } from '../../../services/product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit {
  productForm!: FormGroup;
  isSubmitting = false;
  isEditMode = false;
  
  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private dialogRef: MatDialogRef<ProductFormComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: Product
  ) { }

  ngOnInit(): void {
    this.isEditMode = !!this.data.id;
    
    this.productForm = this.fb.group({
      name: [this.data.name || '', [Validators.required]],
      description: [this.data.description || '', [Validators.required]],
      price: [this.data.price || 0, [Validators.required, Validators.min(0)]],
      quantity: [this.data.quantity || 0, [Validators.required, Validators.min(0)]]
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      return;
    }
    
    this.isSubmitting = true;
    const product: Product = {
      ...this.data,
      ...this.productForm.value
    };
    
    if (this.isEditMode) {
      this.productService.update(this.data.id!, product).subscribe({
        next: () => {
          this.snackBar.open('Product updated successfully', 'Close', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.snackBar.open('Failed to update product. ' + err.error?.message || err.message, 'Close', { duration: 5000 });
        }
      });
    } else {
      this.productService.create(product).subscribe({
        next: () => {
          this.snackBar.open('Product created successfully', 'Close', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.snackBar.open('Failed to create product. ' + err.error?.message || err.message, 'Close', { duration: 5000 });
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
